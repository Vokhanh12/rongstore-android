plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions.add("environment")

    productFlavors {
        create("prod") {
            isDefault = true
        }
        create("mock")
    }

    namespace = "com.aliasadi.domain"
    ndkVersion = "27.1.12297006"
}

kotlin {
    jvmToolchain(17)
}


dependencies {
    implementation(libs.paging.common.ktx)
    implementation(libs.androidx.runtime.annotation)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.common.jvm)

    // Json
    implementation(libs.squareup.moshi)
    implementation(libs.barcode.scanning.common)

}