// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    // Add Google Services plugin
    id("com.google.gms.google-services") version "4.4.0" apply false
    alias(libs.plugins.androidLibrary) apply false

}


buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Add Google Services classpath
        classpath("com.google.gms:google-services:4.4.0")
    }
}