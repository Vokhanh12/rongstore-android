plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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
}

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
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