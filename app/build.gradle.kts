plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)

    val kotlinVersion = "1.9.0"
    kotlin("plugin.serialization") version kotlinVersion
}

android {
    namespace = "com.fakebook.SocialMediaApp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fakebook.SocialMediaApp"
        minSdk = 32
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
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    val supabaseVersion = "3.1.4"
    val ktorVersion = "3.1.2"
    //noinspection UseTomlInstead
    implementation(platform("io.github.jan-tennert.supabase:bom:$supabaseVersion"))
    //noinspection UseTomlInstead
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    //noinspection UseTomlInstead
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    //noinspection UseTomlInstead
    implementation("io.github.jan-tennert.supabase:storage-kt:$supabaseVersion")
}