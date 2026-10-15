
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.veio.browser"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.veio.browser"
        minSdk = 26
        targetSdk = 35

        versionCode = 1
        versionName = "0.1.0"
    }
}
