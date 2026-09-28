plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.saydlawy.ultimatemushaf"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.saydlawy.ultimatemushaf"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    signingConfigs {
        create("release") {
            val store = System.getenv("ULTIMATE_KEYSTORE")
            val password = System.getenv("ULTIMATE_KEYSTORE_PASSWORD")
            val alias = System.getenv("ULTIMATE_KEY_ALIAS")
            val keyPassword = System.getenv("ULTIMATE_KEY_PASSWORD")
            if (!store.isNullOrBlank() && !password.isNullOrBlank() && !alias.isNullOrBlank() && !keyPassword.isNullOrBlank()) {
                storeFile = file(store)
                storePassword = password
                keyAlias = alias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-session:1.5.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
