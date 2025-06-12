// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // اضافه کردن پلاگین Google Services به صورت Kotlin DSL
    id("com.google.gms.google-services") version "4.3.10" apply false
}
