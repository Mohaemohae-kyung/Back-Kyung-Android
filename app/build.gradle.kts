import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

val prodBaseUrl: String = "https://can-fly.shop/"
val debugBaseUrl: String = localProperties.getProperty("BASE_URL", prodBaseUrl)

val tossClientKey: String = "test_ck_GePWvyJnrKmlw5N22DXR3gLzN97E"

android {
    namespace = "kyung.kung_android"
    compileSdk = 36

    defaultConfig {
        applicationId = "kyung.kung_android"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
            }
        }
    }

    signingConfigs {
        val keystoreFile = rootProject.file("Back-Kyung-Android-keystore.jks")
        if (keystoreFile.exists()) {
            create("release") {
                storeFile = keystoreFile
                storePassword = localProperties.getProperty("KEYSTORE_PASSWORD") ?: ""
                keyAlias = localProperties.getProperty("KEY_ALIAS") ?: ""
                keyPassword = localProperties.getProperty("KEY_PASSWORD") ?: ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"$prodBaseUrl\"")
            buildConfigField("Boolean", "SSL_PINNING_ENABLED", "true")
            buildConfigField("String", "PINNING_HOST", "\"can-fly.shop\"")
            buildConfigField("String", "PIN_CURRENT", "\"sha256/S4fHfavKWAJVY+UVx8FovdRLN2QaAJ7IHk+sK1BaTFo=\"")
            buildConfigField("String", "PIN_BACKUP", "\"sha256/y7xVm0TVJNahMr2sZydE2jQH8SquXV9yLF9seROHHHU=\"")
            buildConfigField("String", "TOSS_CLIENT_KEY", "\"$tossClientKey\"")
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
        debug {
            isMinifyEnabled = false
            buildConfigField("String", "BASE_URL", "\"$debugBaseUrl\"")
            buildConfigField("Boolean", "SSL_PINNING_ENABLED", "false")
            buildConfigField("String", "PINNING_HOST", "\"\"")
            buildConfigField("String", "PIN_CURRENT", "\"\"")
            buildConfigField("String", "PIN_BACKUP", "\"\"")
            buildConfigField("String", "TOSS_CLIENT_KEY", "\"$tossClientKey\"")
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
        buildConfig = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    // AndroidX 기본
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose (BOM으로 버전 묶음)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // DI - Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Network - Retrofit + OkHttp + Kotlinx Serialization
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Image (Coil 3)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Splash
    implementation(libs.androidx.core.splashscreen)

    // DataStore (token storage)
    implementation(libs.androidx.datastore.preferences)

    // Payment - 토스페이먼츠 결제 SDK
    implementation("com.github.tosspayments:payment-sdk-android:0.1.22")

    // STOMP WebSocket (krossbow)
    implementation(libs.krossbow.stomp.core)
    implementation(libs.krossbow.websocket.okhttp)
    implementation(libs.krossbow.stomp.kxserialization.json)

    // QR (ZXing)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Firebase (FCM)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
}
