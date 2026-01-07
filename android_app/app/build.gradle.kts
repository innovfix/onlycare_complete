import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    // Firebase enabled for FCM (Incoming Call Notifications)
    id("com.google.gms.google-services")
    // id("com.google.firebase.crashlytics")  // Keep crashlytics disabled for now
    id("com.google.devtools.ksp") version "1.9.22-1.0.16"
}

android {
    namespace = "com.onlycare.app"
    compileSdk = 35
    
    // NDK version for 16KB page alignment support
    ndkVersion = "26.1.10909125"

    defaultConfig {
        // IMPORTANT: Play Console package name (must be unique in your Play account)
        applicationId = "com.onlycare.onlycareapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 13
        versionName = "3.2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystoreProps = Properties().apply {
                val f = rootProject.file("keystore.properties")
                if (f.exists()) {
                    f.inputStream().use { load(it) }
                }
            }

            fun signingProp(name: String): String? {
                return (project.findProperty(name) as String?)
                    ?: keystoreProps.getProperty(name)
                    ?: System.getenv(name)
            }

            val isBuildingRelease = gradle.startParameter.taskNames.any { taskName ->
                taskName.contains("release", ignoreCase = true) ||
                    taskName.contains("bundle", ignoreCase = true)
            }

            if (isBuildingRelease) {
                val required = listOf("KEYSTORE_FILE", "KEYSTORE_PASSWORD", "KEY_ALIAS", "KEY_PASSWORD")
                val missing = required.filter { signingProp(it).isNullOrBlank() }
                if (missing.isNotEmpty()) {
                    throw GradleException(
                        "Missing signing properties: ${missing.joinToString()}.\n" +
                            "Provide them in root `keystore.properties` (recommended, gitignored), " +
                            "or as Gradle properties, or as environment variables."
                    )
                }
            }

            val keystoreFileName = signingProp("KEYSTORE_FILE") ?: "onlycare-release-key.jks"
            // Try project root first (where keystore actually is), then fallback to app folder
            val keystoreFile = file("${project.rootDir}/$keystoreFileName").takeIf { it.exists() }
                ?: file("../$keystoreFileName").takeIf { it.exists() }
                ?: file(keystoreFileName)
            storeFile = keystoreFile
            storePassword = signingProp("KEYSTORE_PASSWORD")
            keyAlias = signingProp("KEY_ALIAS")
            keyPassword = signingProp("KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            // Speed up debug builds
            isMinifyEnabled = false
            isShrinkResources = false
            // Disable crashlytics during debug builds
            manifestPlaceholders["crashlyticsCollectionEnabled"] = false
            // Faster compilation for debug builds
            isDebuggable = true
            isJniDebuggable = false
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = false  // Disabled to fix build error
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        
        create("staging") {
            initWith(getByName("release"))
            isDebuggable = true
            applicationIdSuffix = null  // Same package name as release
            versionNameSuffix = "-staging"
            matchingFallbacks.add("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        // Enable incremental compilation
        freeCompilerArgs += listOf(
            "-Xjvm-default=all",
            "-opt-in=kotlin.RequiresOptIn",  // Updated from deprecated -Xopt-in
            "-Xbackend-threads=8"  // Increased parallel compilation threads
        )
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += listOf(
                "-Xjvm-default=all",
                "-opt-in=kotlin.RequiresOptIn",  // Updated from deprecated -Xopt-in
                "-Xbackend-threads=8"
            )
        }
    }

    buildFeatures {
        compose = true
        // Needed to use BuildConfig.DEBUG in Kotlin sources
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    // Optimize build performance
    androidResources {
        // Skip unused resources during development
        ignoreAssetsPattern = "!.svn:!.git:!.ds_store:!*.scc:.*:!CVS:!thumbs.db:!picasa.ini:!*~"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            // CRITICAL: Must be false for 16KB alignment support
            // AGP 8.5.1+ automatically aligns uncompressed native libraries on 16KB boundaries
            useLegacyPackaging = false
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a")  // Only include most common architectures
            isUniversalApk = false  // Create separate APKs per ABI
        }
    }

    bundle {
        abi {
            enableSplit = true  // Optimize bundle size with ABI splits
        }
    }
}

// KSP Configuration for faster builds
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
    // Hilt optimizations to speed up KSP processing
    arg("dagger.fastInit", "enabled")
    arg("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
}

// Additional optimization for KSP tasks
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        // Suppress warnings for faster compilation
        freeCompilerArgs.addAll(
            "-Xskip-prerelease-check",
            "-Xsuppress-version-warnings"
        )
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Compose
    // Updated to avoid Compose hover handling crash on newer Samsung/Android builds.
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Truecaller (Auto phone verification / mobile number detection)
    implementation("com.truecaller.android.sdk:truecaller-sdk:3.0.0")
    
    // WebSocket - Socket.io for real-time call signaling
    implementation("io.socket:socket.io-client:2.1.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Firebase - Enabled for FCM (Incoming Call Notifications)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging")  // For push notifications
    // Optional: Uncomment below if needed later
    // implementation("com.google.firebase:firebase-auth")
    // implementation("com.google.firebase:firebase-firestore")
    // implementation("com.google.firebase:firebase-analytics")
    // implementation("com.google.firebase:firebase-crashlytics")

    // Image Loading
    implementation("com.github.bumptech.glide:compose:1.0.0-beta01")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Accompanist
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Lottie (animated illustrations)
    implementation("com.airbnb.android:lottie-compose:6.4.1")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")

    // Agora SDK (Video/Audio Calling) - MATCHED TO HIMA (WORKING VERSION)
    // Using full SDK for audio and video calls
    // Using 4.5.0 - same version as working hima project
    implementation("io.agora.rtc:full-sdk:4.5.0")

    // OneSignal
    implementation("com.onesignal:OneSignal:5.0.0")

    // Coil (Image Loading Alternative)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

