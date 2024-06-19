plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.omrifit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.omrifit"
        minSdk = 28
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures{
        viewBinding =true
    }
}

dependencies {
    implementation ("com.google.guava:guava:31.0.1-android")

    // To use CallbackToFutureAdapter
    implementation ("androidx.concurrent:concurrent-futures:1.1.0")
    implementation ("org.tensorflow:tensorflow-lite:2.10.0")// Kotlin
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.6.0")
    implementation ("com.google.android.material:material:1.4.0")

    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")
implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.generativeai)
    implementation(libs.androidx.activity)
    implementation(fileTree(mapOf(
        "dir" to "C:\\Users\\HP\\AppData\\Local\\Android\\Sdk\\platforms\\android-34",
        "include" to listOf("*.aar", "*.jar"),
    )))
    implementation(fileTree(mapOf(
        "dir" to "C:\\Users\\HP\\AppData\\Local\\Android\\Sdk\\platforms\\android-UpsideDownCakePrivacySandbox",
        "include" to listOf("*.aar", "*.jar"),
    )))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation ("app.rive:rive-android:5.0.0")
    implementation ("androidx.startup:startup-runtime:1.1.1")
    implementation ("com.android.volley:volley:1.1.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.github.sundeepk:compact-calendar-view:3.0.0")
    implementation("com.paypal.sdk:paypal-android-sdk:2.14.2")
    implementation("com.razorpay:checkout:1.6.4")
    implementation("com.android.volley:volley:1.2.0")
    testImplementation("junit:junit:4.12")
    implementation("de.hdodenhof:circleimageview:3.0.0")
    testImplementation("junit:junit:4.12")
    implementation("de.hdodenhof:circleimageview:3.0.0")
    implementation("com.devbrackets.android:exomedia:5.0.0")
    androidTestImplementation ("androidx.test.ext:junit:1.1.3")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.4.0")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("androidx.work:work-runtime:2.7.1")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.google.firebase:firebase-auth")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth-ktx:22.1.2")
    implementation("com.google.firebase:firebase-auth:22.1.2")
    implementation("com.google.firebase:firebase-database:20.2.2")
    implementation("com.google.firebase:firebase-storage:20.2.1")
    implementation("androidx.biometric:biometric:1.1.0")
    testImplementation("junit:junit:4.13.2")
    implementation("uk.co.samuelwall:material-tap-target-prompt:3.3.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("androidx.navigation:navigation-ui:2.7.3")// Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.android.gms:play-services-fitness:21.1.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.google.android.material:material:1.5.0")  // או הגרסה העדכנית ביותר
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.jjoe64:graphview:4.2.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:xx.x.x")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-auth:20.1.0")
    implementation("com.google.firebase:firebase-auth:22.2.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.navigation:navigation-fragment:2.7.6")
    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
    testImplementation ("junit:junit:4.13.2")

    // AndroidX Test - Instrumentation testing
    androidTestImplementation ("androidx.test.ext:junit:1.1.3")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation ("androidx.test:runner:1.4.0")
    androidTestImplementation ("androidx.test:rules:1.4.0")

    val billing_version = "6.1.0"
    implementation("com.android.billingclient:billing-ktx:$billing_version")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:2.9.9")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.github.blackfizz:eazegraph:1.2.5l@aar")
    implementation("com.nineoldandroids:library:2.4.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.aallam.openai:openai-client:3.0.0")
    implementation("io.ktor:ktor-client-android:2.2.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.google.android.material:material:1.2.1")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.mlkit:smart-reply:17.0.2")
    implementation("com.jjoe64:graphview:4.2.2")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
    implementation("com.google.android.material:material:1.4.0")
    implementation("com.google.android.gms:play-services-mlkit-smart-reply:16.0.0-beta1")
    implementation("com.google.mlkit:smart-reply:17.0.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.burhanrashid52:photoeditor:3.0.2")
    implementation ("androidx.core:core:1.3.0")
}