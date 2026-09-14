import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.hilt.android.gradle.plugin)
    id("com.google.android.gms.oss-licenses-plugin")
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

val properties = Properties()
val localProperties = rootProject.file("local.properties")
if (localProperties.exists()) {
    properties.load(localProperties.inputStream())
}

android {
    namespace = "com.ar9988.tagfilemanager"
    compileSdk = 36

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("release-key.jks")
            storePassword = properties.getProperty("STORE_PASSWORD")
            keyAlias = properties.getProperty("KEY_ALIAS")
            keyPassword = properties.getProperty("KEY_PASSWORD")
        }
    }

    defaultConfig {
        applicationId = "com.ar9988.tagfilemanager"
        minSdk = 26
        targetSdk = 36
        versionCode = 20
        versionName = "1.1.9"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            debugSymbolLevel = "FULL"
        }

        /*
         * 스캔 프로파일 로그 스위치.
         *
         * 어떤 빌드 타입에서도 켤 수 있다. 특히 릴리스에서 재야 의미가 있는데,
         * 디버그 빌드는 LeakCanary 가 강제 GC 를 돌리고 ART 최적화도 일부 꺼져 있어서
         * 스캔처럼 할당이 많은 작업의 시간을 크게 부풀리기 때문이다.
         *
         *   ./gradlew :app:assembleRelease -PscanProfiling=true
         *   adb logcat -s ScanProfile
         *
         * 기본값이 false 라 실수로 켜진 채 배포될 일은 없다.
         * 또한 상수이므로, 꺼져 있으면 R8 이 로그 호출 자체를 지운다.
         */
        buildConfigField(
            "boolean",
            "SCAN_PROFILING",
            (project.findProperty("scanProfiling") as String? ?: "false")
        )

        /*
         * 스캔 시 한 폴더 안의 파일을 동시에 몇 개까지 처리할지.
         *
         * 기기·저장장치마다 최적값이 달라 실측으로만 정할 수 있어서 밖으로 뺐다.
         *
         *   ./gradlew :app:assembleRelease -PscanParallelism=2
         */
        buildConfigField(
            "int",
            "SCAN_PARALLELISM",
            (project.findProperty("scanParallelism") as String? ?: "4")
        )
    }

    buildTypes {
        debug {
            // 디버그에서는 늘 켜 둔다. 다만 이 숫자로 성능을 판단하면 안 된다 — 위 주석 참고.
            buildConfigField("boolean", "SCAN_PROFILING", "true")
        }

        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    debugImplementation(libs.leakcanary.android)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.firebase.crashlytics)
    ksp(libs.androidx.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":local-source"))
    implementation(project(":di-bridge"))

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.coil.compose)
    implementation(libs.coil.video)
    implementation(libs.coil.svg)
    implementation(libs.coil.video)
    implementation(libs.coil.gif)

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    implementation(libs.play.services.oss.licenses)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.compose.material.icons.extended)
}
