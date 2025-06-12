plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // فعال‌سازی پلاگین Google Services
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
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.activity:activity-ktx:1.2.4")
    implementation("androidx.constraintlayout:constraintlayout:2.1.1")
    implementation(libs.activity)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.espresso:espresso-core:3.4.0")

    // Firebase dependencies
    implementation("com.google.firebase:firebase-auth:19.3.0")  // نسخه قدیمی‌تر Firebase Authentication
    implementation("com.google.firebase:firebase-firestore:21.4.0")  // نسخه قدیمی‌تر Firebase Firestore
    implementation("com.google.firebase:firebase-database:19.5.1")  // نسخه قدیمی‌تر Firebase Realtime Database
}
