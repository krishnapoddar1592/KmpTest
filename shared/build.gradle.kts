import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.skie)
}

android {
    namespace = "com.reflect.app"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
    }
}

version = "1.2"

kotlin {
    jvmToolchain(11)
    // https://kotlinlang.org/docs/multiplatform-expect-actual.html#expected-and-actual-classes
    // To suppress this warning about usage of expected and actual classes
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    androidTarget {
        @Suppress("OPT_IN_USAGE")
        unitTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            isStatic = false
            linkerOpts("-lsqlite3")
            export(libs.touchlab.kermit.simple)
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlin.time.ExperimentalTime")
            }
        }

        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.koin.view.model)
            implementation(libs.coroutines.core)
            implementation(libs.sqlDelight.coroutinesExt)
            implementation(libs.bundles.ktor.common)
            implementation(libs.multiplatformSettings.common)
            implementation(libs.kotlinx.dateTime)
            implementation(libs.touchlab.skie.annotations)
            api(libs.touchlab.kermit)
//            implementation(libs.play.services.mlkit.face.detection)

        }
        commonTest.dependencies {
            implementation(libs.bundles.shared.commonTest)
        }
        androidMain.dependencies {
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.sqlDelight.android)
            implementation(libs.ktor.client.okHttp)
            implementation(libs.play.services.fitness)

        }
        getByName("androidUnitTest").dependencies {
            implementation(libs.bundles.shared.androidTest)
        }
        iosMain.dependencies {
            implementation(libs.sqlDelight.native)
            implementation(libs.ktor.client.ios)
            api(libs.touchlab.kermit.simple)
        }
    }
}
dependencies {
    implementation(libs.firebase.auth.ktx)
    // Add TensorFlow Lite dependencies
//    implementation(libs.tensorflow.lite)
//    implementation(libs.tensorflow.lite.gpu) // Optional GPU acceleration
//    implementation(libs.tensorflow.lite.support)
    implementation(libs.litert)

    // For image processing
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.litert.gpu)
    implementation(libs.firebase.firestore)
    implementation(libs.vision.common)
    implementation(libs.play.services.mlkit.face.detection)
    implementation(libs.androidx.media3.common.ktx)
}

sqldelight {
    databases.create("ReflectDb") {
        packageName.set("com.reflect.app.db")
    }
}
