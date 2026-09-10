<#
.SYNOPSIS
  Print an Android device's current thermal/performance state for scan benchmarks.

.EXAMPLE
  .\tools\android-device-status.ps1
  .\tools\android-device-status.ps1 -Serial R58N123ABC
#>
[CmdletBinding()]
param(
    [string]$Serial
)

$ErrorActionPreference = 'Stop'
if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
    throw 'adb was not found. Add Android SDK platform-tools to PATH and try again. (Example: <Android SDK>\platform-tools)'
}

function Invoke-AdbShell {
    param([Parameter(Mandatory = $true)][string]$Command)

    $arguments = @()
    if ($Serial) { $arguments += @('-s', $Serial) }
    # Feed the command through stdin instead of `adb shell sh -c <command>`.
    # Windows argument parsing otherwise splits quoted shell pipelines/semicolons.
    $arguments += @('shell', 'sh')
    # Some OEM thermal services emit harmless Binder diagnostics to stderr.
    # Do not let PowerShell promote that stderr output to a terminating error;
    # adb's exit code below remains the failure signal.
    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $result = $Command | & adb @arguments 2>$null
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    if ($exitCode -ne 0) { throw "adb shell failed: $result" }
    return ($result | Out-String).Trim()
}

function Get-FirstValue {
    param([Parameter(Mandatory = $true)][string[]]$Commands)

    foreach ($command in $Commands) {
        try {
            $value = Invoke-AdbShell $command
            if ($value -and $value -notmatch 'No such file|Permission denied|not found') {
                return $value
            }
        } catch { }
    }
    return 'N/A'
}

function Convert-Temperature {
    param([string]$Value)
    $number = 0.0
    if ([double]::TryParse($Value, [ref]$number)) {
        if ($number -gt 1000) { return ('{0:N1} C' -f ($number / 1000)) }
        if ($number -gt 200) { return ('{0:N1} C' -f ($number / 10)) }
        return ('{0:N1} C' -f $number)
    }
    return $Value
}

$devices = & adb devices 2>&1
if ($LASTEXITCODE -ne 0) { throw "adb를 실행할 수 없습니다. Android SDK platform-tools가 PATH에 있는지 확인하세요. $devices" }

$online = $devices | Where-Object { $_ -match "`tdevice$" }
if (-not $online) { throw 'No authorized Android device was found. Connect the device, allow USB debugging, then check `adb devices`.' }
if (-not $Serial -and $online.Count -gt 1) { throw 'More than one device is connected. Specify -Serial <adb serial>.' }

$model = (Invoke-AdbShell 'getprop ro.product.manufacturer; getprop ro.product.model') -split "`r?`n" | Where-Object { $_ }
$sdk = Invoke-AdbShell 'getprop ro.build.version.sdk'
$android = Invoke-AdbShell 'getprop ro.build.version.release'
$battery = Invoke-AdbShell 'dumpsys battery'
$batteryLevel = [regex]::Match($battery, '(?m)^\s*level:\s*(\d+)').Groups[1].Value
$batteryStatus = [regex]::Match($battery, '(?m)^\s*status:\s*(\d+)').Groups[1].Value
$batteryTempRaw = [regex]::Match($battery, '(?m)^\s*temperature:\s*(\d+)').Groups[1].Value
$batteryTemp = if ($batteryTempRaw) { Convert-Temperature $batteryTempRaw } else { 'N/A' }
$charging = switch ($batteryStatus) { '2' { 'charging' }; '5' { 'full' }; '3' { 'discharging' }; '4' { 'not charging' }; default { 'unknown' } }

$cpuMax = Get-FirstValue @('cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq', 'cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq')
$cpuCurrent = Get-FirstValue @('cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq', 'cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq')
$cpuGovernor = Get-FirstValue @('cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor')
$thermalStatus = Get-FirstValue @('dumpsys thermalservice | grep -iE "status|thrott|temperature" | head -40')
$gpu = Get-FirstValue @('cat /sys/class/kgsl/kgsl-3d0/gpu_busy_percentage; cat /sys/class/kgsl/kgsl-3d0/devfreq/cur_freq', 'cat /sys/class/devfreq/*gpu*/cur_freq 2>/dev/null')
$thermalZones = Get-FirstValue @('for z in /sys/class/thermal/thermal_zone*; do printf "%s=" "$(cat "$z/type" 2>/dev/null)"; cat "$z/temp" 2>/dev/null; done | head -30')

$cpuMaxMHz = if ($cpuMax -match '^\d+$') { [math]::Round([double]$cpuMax / 1000) } else { $null }
$cpuCurrentMHz = if ($cpuCurrent -match '^\d+$') { [math]::Round([double]$cpuCurrent / 1000) } else { $null }
$thermalText = "$thermalStatus`n$thermalZones"
$thermalStatusMatch = [regex]::Match($thermalStatus, '(?m)^\s*Thermal Status:\s*(\d+)')
$thermalStatusCode = if ($thermalStatusMatch.Success) { [int]$thermalStatusMatch.Groups[1].Value } else { $null }
$severity = 'Inconclusive'
$reason = 'The device does not expose thermal status or CPU policy information.'
if ($null -ne $thermalStatusCode -and $thermalStatusCode -ge 3) {
    $severity = 'High likelihood of throttling'
    $reason = "Android Thermal Status is $thermalStatusCode (3=SEVERE, 4=CRITICAL, 5=EMERGENCY, 6=SHUTDOWN)."
} elseif ($null -ne $thermalStatusCode -and $thermalStatusCode -ge 1) {
    $severity = 'Possible throttling'
    $reason = "Android Thermal Status is $thermalStatusCode (1=LIGHT, 2=MODERATE)."
} elseif ($thermalText -match '(?i)(severe|critical|emergency|shutdown)') {
    $severity = 'High likelihood of throttling'
    $reason = 'Thermal output contains a severe state or throttling signal.'
} elseif ($thermalText -match '(?i)\b(light|moderate)\b') {
    $severity = 'Possible throttling'
    $reason = 'Thermal output contains a warning or throttling signal.'
} elseif ($cpuMaxMHz -and $cpuCurrentMHz -and $cpuCurrentMHz -lt ($cpuMaxMHz * 0.55)) {
    $severity = 'Needs observation'
    $reason = "cpu0 is below 55% of its reported maximum ($cpuCurrentMHz/$cpuMaxMHz MHz). This can be idle power saving; re-check under scan load."
} else {
    $severity = 'No clear throttling signal'
    $reason = 'No limiting condition appears in the currently readable thermal/CPU signals.'
}

Write-Host ''
Write-Host '=== Android scan benchmark device status ===' -ForegroundColor Cyan
Write-Host ("Device       : {0}" -f ($model -join ' '))
Write-Host ("Android      : {0} (SDK {1})" -f $android, $sdk)
$batteryLevelDisplay = if ($batteryLevel) { $batteryLevel } else { 'N/A' }
$cpuCurrentDisplay = if ($null -ne $cpuCurrentMHz) { $cpuCurrentMHz } else { $cpuCurrent }
$cpuMaxDisplay = if ($null -ne $cpuMaxMHz) { $cpuMaxMHz } else { $cpuMax }
Write-Host ("Battery      : {0}% / {1} / {2}" -f $batteryLevelDisplay, $charging, $batteryTemp)
Write-Host ("CPU cpu0     : current {0} MHz / max {1} MHz / governor {2}" -f $cpuCurrentDisplay, $cpuMaxDisplay, $cpuGovernor)
Write-Host ("GPU          : {0}" -f $gpu)
Write-Host ''
Write-Host ("Throttle verdict: {0}" -f $severity) -ForegroundColor Yellow
Write-Host ("Why          : {0}" -f $reason)
Write-Host ''
Write-Host '--- thermalservice (if exposed) ---' -ForegroundColor DarkCyan
Write-Host $thermalStatus
Write-Host '--- thermal zones (raw device values; if exposed) ---' -ForegroundColor DarkCyan
Write-Host $thermalZones
Write-Host ''
Write-Host 'Tip: Run the scan for 1-2 minutes, then run this again. Confirm throttling only when thermal state rises and clocks stay reduced under load.' -ForegroundColor DarkGray
