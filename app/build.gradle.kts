plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("io.realm.kotlin")
}

android {
    namespace = "com.example.tabsgpttutor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tabsgpttutor"
        minSdk = 30
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
    implementation ("com.kizitonwose.calendar:view:2.3.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation ("androidx.fragment:fragment-ktx:1.6.2")
    implementation ("androidx.preference:preference-ktx:1.2.1")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.7")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("io.realm.kotlin:library-base:1.16.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0")
}