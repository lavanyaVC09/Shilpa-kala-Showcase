plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.shilpakalashowcase"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.shilpakalashowcase"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}
 dependencies {
         // ... other imports
         implementation("io.coil-kt.coil3:coil-compose:3.0.0")

         // 🔹 ADD THIS LINE IF IT IS MISSING:
         implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0")

        // Core Android & Lifecycle
        implementation("androidx.core:core-ktx:1.13.1")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
        implementation("androidx.activity:activity-compose:1.9.0")

        // ✅ COIL (The library that makes images visible)
        // Coil 2.6.0 is stable for your current Kotlin/Compose version
        implementation("io.coil-kt:coil-compose:2.6.0")

        // Compose UI (Using the BOM ensures versions match)
        implementation(platform("androidx.compose:compose-bom:2024.04.01"))
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.ui:ui-graphics")
        implementation("androidx.compose.ui:ui-tooling-preview")
        implementation("androidx.compose.material3:material3")
        implementation("androidx.compose.animation:animation")

        // ✅ FIREBASE (For your 20+ sculptures)
        implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
        implementation("com.google.firebase:firebase-firestore-ktx")

        // Testing
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

        // Debug
        debugImplementation("androidx.compose.ui:ui-tooling")
        debugImplementation("androidx.compose.ui:ui-test-manifest")
    }