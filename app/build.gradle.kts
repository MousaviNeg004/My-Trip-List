plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "ir.shariaty.mytriplist"
    compileSdk = 35
    buildToolsVersion = "35.0.1"

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
    implementation("androidx.activity:activity-ktx:1.6.1") // ✅ ارتقا برای سازگاری بهتر
    implementation("androidx.constraintlayout:constraintlayout:2.1.1")
    implementation("com.google.code.gson:gson:2.10.1")
    // Lifecycle هماهنگ
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")

    // تست‌ها
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("com.google.firebase:firebase-firestore:24.9.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-messaging:23.1.1")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.2.1")
}