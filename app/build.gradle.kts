plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("com.chaquo.python")
}

android {
    namespace = "dev.wander.android.opentagviewer"
    compileSdk = 35

    defaultConfig {
        val maptilerKey = (System.getenv("MAPTILER_API_KEY") ?: project.findProperty("MAPTILER_API_KEY") ?: "").toString()
        buildConfigField("String", "MAPTILER_API_KEY", "\"$maptilerKey\"")
        val maptilerKey = (System.getenv("MAPTILER_API_KEY") ?: project.findProperty("MAPTILER_API_KEY") ?: "").toString()
        buildConfigField("String", "MAPTILER_API_KEY", "\"$maptilerKey\"")
        applicationId = "dev.wander.android.opentagviewer"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val maptilerKey = (System.getenv("MAPTILER_API_KEY") ?: project.findProperty("MAPTILER_API_KEY") ?: "").toString()
        buildConfigField("String", "MAPTILER_API_KEY", "\"$maptilerKey\"")
    }

    buildFeatures {
        buildConfig = true
        buildConfig = true
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    python {
        version = "3.11"
        pip {
            install("FindMy==0.7.6")
            install("NSKeyedUnArchiver==1.5")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("org.maplibre.gl:android-sdk:11.5.1")
    implementation("org.maplibre.gl:android-sdk:11.5.1")
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation("org.maplibre.gl:android-sdk:11.5.1")
}
