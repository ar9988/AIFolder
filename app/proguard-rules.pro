# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# PDFBox 관련 라이브러리 난독화 방지
-keep class com.tom_roush.pdfbox.** { *; }

# Gemalto JP2Decoder 관련 경고 무시
-dontwarn com.gemalto.jp2.**
-keep class ai.onnxruntime.** { *; }
-keep class ai.onnxruntime.native.** { *; }
-dontwarn ai.onnxruntime.**