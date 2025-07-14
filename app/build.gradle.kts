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
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database:19.5.1")  // نسخه قدیمی‌تر Firebase Realtime Database
    implementation("com.google.firebase:firebase-auth:21.0.0")
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-firestore:24.1.1")
    implementation("com.google.firebase:firebase-storage:20.0.0")
    implementation ("com.google.firebase:firebase-messaging:23.1.1") // برای Firebase Cloud Messaging
    implementation ("com.google.firebase:firebase-core:20.1.0") // برای Firebase Core

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // Other dependencies
    implementation("androidx.appcompat:appcompat:1.3.1")

}
