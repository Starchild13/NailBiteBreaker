// App-level build script — declares all dependencies and Android build config.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace   = "com.nailbitebreaker"
    compileSdk  = 37

    defaultConfig {
        applicationId = "com.nailbitebreaker"
        minSdk        = 26
        targetSdk     = 37
        versionCode   = 12
        versionName   = "1.0.12"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            // Enable R8 shrinking, obfuscation, and optimization.
            isMinifyEnabled = true
            // Enable unused resource stripping.
            isShrinkResources = true
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // For internal testing, we'll keep using the debug config unless you've set up a release key.
            signingConfig = signingConfigs.getByName("debug")

            // Fix for "App Bundle contains native code, and you've not uploaded debug symbols"
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        
        debug {
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures { compose = true }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)

    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.coroutines.android)

    implementation(libs.lottie.compose)

    debugImplementation(libs.compose.ui.tooling)

    // Jetpack XR SDK
    implementation(libs.xr.runtime)
    implementation(libs.xr.scenecore)
    implementation(libs.xr.compose)
    implementation(libs.xr.material3)
    
    // Required if you use ProGuard/Minification with XR
    compileOnly(libs.xr.extensions)

    // Adaptive Layouts
    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.adaptive.layout)
    implementation(libs.androidx.adaptive.navigation)
    implementation(libs.androidx.adaptive.navigation.suite)
    implementation(libs.androidx.window.core)

    // RevenueCat
    implementation(libs.revenuecat.purchases)
    implementation(libs.revenuecat.purchases.ui)
    implementation(libs.revenuecat.purchases.admob)
    implementation(libs.play.services.ads)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
