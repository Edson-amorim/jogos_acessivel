plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.edson.dominoacessivel"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.edson.dominoacessivel"
        minSdk = 21
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"
        resourceConfigurations += listOf("pt", "pt-rBR")
    }

    signingConfigs {
        create("release") {
            storeFile = file("../keystore/minha-chave-release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "android123"
            keyAlias = System.getenv("KEY_ALIAS") ?: "chave_app"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "android123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
