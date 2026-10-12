import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.dssmv.plantkeep"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.dssmv.plantkeep"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        buildConfig = true
    }
}

// Lê o local.properties e expõe as credenciais do restdb via BuildConfig (nunca vão para git)
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    defaultConfig {
        buildConfigField("String", "RESTDB_APIKEY", "\"${localProps.getProperty("restdb.apikey", "")}\"")
        buildConfigField("String", "RESTDB_DBNAME", "\"${localProps.getProperty("restdb.dbname", "")}\"")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
