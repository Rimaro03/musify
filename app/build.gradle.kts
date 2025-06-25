import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    val keystoreFile = project.rootProject.file("local.properties")
    val properties = Properties()
    properties.load(keystoreFile.inputStream())

    val clientId = properties.getProperty("clientId") ?: ""
    val clientSecret = properties.getProperty("clientSecret") ?: ""

    namespace = "com.rimaro.musify"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rimaro.musify"
        minSdk = 29
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
            buildConfigField("String", "CLIENT_ID", "\"" + clientId + "\"")
            buildConfigField("String", "CLIENT_SECRET", "\"" + clientSecret + "\"")
        }
        debug {
            buildConfigField("String", "CLIENT_ID", "\"" + clientId + "\"")
            buildConfigField("String", "CLIENT_SECRET", "\"" + clientSecret + "\"")
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
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
    // ViewModel utilities for Compose
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.1")

    implementation("com.github.teamnewpipe:NewPipeExtractor:0.24.6")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // media3
    implementation("androidx.media3:media3-exoplayer:1.7.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.7.1")
    implementation("androidx.media3:media3-ui:1.7.1")
    implementation("androidx.media3:media3-common:1.7.1")
    implementation("androidx.media3:media3-session:1.7.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation(project(":spotify-sdk"))
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.spotify.android:auth:1.2.5") // Maven dependency
    // All other dependencies for your app should also be here:
    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.appcompat:appcompat:1.7.1")

    // Retrofit
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")


    // Hilt
    implementation("com.google.dagger:hilt-android:2.56.2")
    ksp("com.google.dagger:hilt-android-compiler:2.56.2")
}