plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.pharmacystore"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pharmacystore"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val apiKey = providers.gradleProperty("MAPS_API_KEY").orNull ?: ""

        // 1) BuildConfig.API_KEY dostępny w kodzie
        buildConfigField("String", "MAPS_API_KEY", "\"$apiKey\"")

        // 2) Placeholder do AndroidManifest.xml (użyje go meta-data)
        manifestPlaceholders["MAPS_API_KEY"] = apiKey
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
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
    buildFeatures {
        compose = true
        buildConfig = true
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
    implementation(libs.androidx.ui.text.google.fonts)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Navigation Component (NavHost + UI)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Coil – ładowanie obrazów
    implementation(libs.coil)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)

    ksp(libs.hilt.android.compiler)

    ksp(libs.androidx.room.compiler)

    implementation(libs.logging.interceptor)

    implementation(libs.androidx.ui.text.google.fonts)

    implementation(libs.firebase.auth)

    implementation(platform(libs.firebase.bom))

    implementation(libs.androidx.room.runtime)

    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.firebase.firestore)

    implementation(libs.androidx.datastore.preferences)


    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.material3)

    implementation(libs.androidx.graphics.shapes)


    // maps
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation(libs.places)
    implementation(libs.android.maps.utils) // clustering + SphericalUtil

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.moshi)
    implementation(libs.converter.moshi)

    implementation(libs.moshi.kotlin)


    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.room.paging)

    implementation(libs.placeholder)

    implementation(libs.kotlinx.datetime)

    implementation(libs.zxing.android.embedded)
    implementation(libs.core)

}