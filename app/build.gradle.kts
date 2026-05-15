import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

/**
 * Scan API Base URL (must end with `/`). Matches Swagger "Servers": http://api.app-fresh.com
 * Retrofit paths: `api/v1/scan/...` from [ScanApiService].
 * Override in `local.properties`: SCAN_API_BASE_URL=...
 */
val scanApiBaseUrl =
    localProperties
        .getProperty("SCAN_API_BASE_URL", "http://api.app-fresh.com/")
        .trim()
        .let { if (it.endsWith("/")) it else "$it/" }

/** Sent as multipart field `userId` until auth is wired. Override with SCAN_API_USER_ID= */
val scanApiUserId = localProperties.getProperty("SCAN_API_USER_ID", "0").trim()

fun escapeForBuildConfig(value: String): String =
    value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")

android {
    namespace = "com.example.myfrigelocal"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myfrigelocal"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SCAN_API_BASE_URL", "\"${escapeForBuildConfig(scanApiBaseUrl)}\"")
        buildConfigField("String", "SCAN_API_USER_ID", "\"${escapeForBuildConfig(scanApiUserId)}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.8.2")

    // CameraX
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    // Image loading for result preview
    implementation("io.coil-kt:coil-compose:2.6.0")

    // ML Kit (Barcode scanning)
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // EXIF orientation (for correct crop mapping)
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // Scan backend (multipart image upload)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    testImplementation(libs.junit)
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Coroutines (이미 있으면 스킵)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
}