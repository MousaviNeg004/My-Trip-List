plugins {
    id("com.android.application")
    id("com.google.gms.google-services")  // فعال‌سازی پلاگین Google Services
}

android {
    namespace = "ir.shariaty.mytriplist"
    compileSdk = 35

    defaultConfig {
        applicationId = "ir.shariaty.mytriplist"
        minSdk = 27
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // وابستگی‌های Firebase
    implementation("com.google.firebase:firebase-auth:21.0.7")  // Firebase Authentication
    implementation("com.google.firebase:firebase-firestore:24.0.5")  // Firebase Firestore
    implementation("com.google.firebase:firebase-database:20.0.5")  // Firebase Realtime Database
}
apply(plugin = "com.google.gms.google-services")
