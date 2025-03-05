plugins {
    alias(libs.plugins.android.application)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.smarthome"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.smarthome"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"${project.findProperty("OPENWEATHER_API_KEY")}\"")
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    aaptOptions{
        noCompress ("tflite")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.database)

    // MQTT dependency
    implementation(libs.org.eclipse.paho.client.mqttv3)

    implementation (libs.face.detection.v1617)

    implementation(libs.ui.graphics)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.core.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.litert)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.androidx.lifecycle.process)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation (libs.androidx.material)
    implementation(libs.androidx.ui.v178) // Pastikan versi terbaru
    implementation(libs.androidx.compose.ui.ui.graphics) // Untuk Brush dan warna

    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    implementation(libs.coil.compose)

    implementation(libs.androidx.navigation.compose)
    implementation (libs.androidx.material.icons.extended)

    implementation (libs.androidx.datastore.preferences)

    implementation(libs.coil)

    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")

// CameraX core library using the camera2 implementation
    val camerax_version = "1.5.0-alpha04"
    // The following line is optional, as the core library is included indirectly by camera-camera2
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    // If you want to additionally use the CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    // If you want to additionally use the CameraX VideoCapture library
    implementation("androidx.camera:camera-video:${camerax_version}")
    // If you want to additionally use the CameraX View class
    implementation("androidx.camera:camera-view:${camerax_version}")
    // If you want to additionally add CameraX ML Kit Vision Integration
    implementation("androidx.camera:camera-mlkit-vision:${camerax_version}")
    // If you want to additionally use the CameraX Extensions library
    implementation("androidx.camera:camera-extensions:${camerax_version}")

    implementation ("com.google.guava:guava:32.1.2-android")

    implementation (libs.jbcrypt)

    implementation(libs.androidx.security.crypto)

    implementation ("io.github.ehsannarmani:compose-charts:0.1.2")
}

kapt {
    correctErrorTypes = true
}