plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.reflect.app.android"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "com.reflect.app.android"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    lint {
        warningsAsErrors = false
        abortOnError = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    aaptOptions {
        noCompress("tflite")
    }
//    packagingOptions {
//        pickFirst("lib/arm64-v8a/libtensorflowlite_gpu_jni.so")
//    }
    packaging {
        resources {
            pickFirsts += setOf(
                "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            )
        }
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(projects.shared)
    implementation(libs.bundles.app.ui)
    implementation(libs.multiplatformSettings.common)
    implementation(libs.kotlinx.dateTime)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.firebase.auth.ktx)
    coreLibraryDesugaring(libs.android.desugaring)
    implementation(libs.koin.android)
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.mlkit:face-detection:16.1.5")

//    implementation(libs.koin.androidx.viewmodel)
//    implementation(libs.koin.androidx.compose)
}