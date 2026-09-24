plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.drivesense.app"
    // Defaults meet the assignment's Android 14–16 / API 34–36 scope.
    // Properties let a lab machine with only API 34 installed run a source build.
    compileSdk = providers.gradleProperty("driveSenseCompileSdk").orNull?.toInt() ?: 36

    defaultConfig {
        applicationId = "com.drivesense.app"
        minSdk = 34
        targetSdk = providers.gradleProperty("driveSenseTargetSdk").orNull?.toInt() ?: 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
