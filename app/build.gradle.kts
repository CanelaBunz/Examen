plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt") // Solo declara el ID, sin versión
}

android {
    namespace = "com.example.examen_2ndo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.examen_2ndo"
        minSdk = 25
        targetSdk = 35
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
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Google Services
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Image Loading
    implementation(libs.glide)
    kapt(libs.glide.compiler) // Usa kapt para Glide

    // UI
    implementation(libs.androidx.recyclerview)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // Room components
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler) // Usa kapt para Room
    implementation(libs.androidx.room.ktx)

    // Lifecycle components
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}