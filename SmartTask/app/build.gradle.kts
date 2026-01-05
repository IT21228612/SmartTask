import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
}


android {
    namespace = "com.smarttask.app"
    compileSdk = 34

    buildFeatures {
        buildConfig = true // ✅ enable custom BuildConfig fields
    }

    defaultConfig {
        applicationId = "com.smarttask.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        // ✅ Kotlin DSL version to load secrets.properties
        val secretsPropsFile = rootProject.file("app/secrets.properties")
        val mapsApiKey = if (secretsPropsFile.exists()) {
            val secretsProps = Properties()
            FileInputStream(secretsPropsFile).use { secretsProps.load(it) }
            secretsProps.getProperty("GOOGLE_MAPS_API_KEY")
        } else {
            "YOUR_API_KEY_HERE"
        }

        // Pass the key to the manifest
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = mapsApiKey
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.room.runtime)
    implementation(libs.room.common.jvm)
    annotationProcessor(libs.room.compiler)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    implementation(libs.work.runtime)
    implementation(libs.lifecycle.process)
    implementation(libs.lifecycle.runtime)
<<<<<<< Updated upstream
=======
    implementation(libs.lifecycle.common.java8)

>>>>>>> Stashed changes
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
