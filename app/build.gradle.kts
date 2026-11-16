plugins {
    id("com.android.application")
}

android {
    namespace = "io.github.ssiloh.morsevibe"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.ssiloh.morsevibe"
        minSdk = 26 // VibrationEffect.createWaveform with per-segment amplitudes (API 26)
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Debug-signed so the CI release APK is installable. Use your own key for distribution.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint { abortOnError = false }
}
