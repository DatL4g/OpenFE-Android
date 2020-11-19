// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    repositories {
        google()
        jcenter()
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.gradle}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.navigation}")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${Versions.hilt}")
        classpath("org.jmailen.gradle:kotlinter-gradle:${Versions.kotlinter}")
        classpath("io.michaelrocks:paranoid-gradle-plugin:${Versions.paranoid}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Versions.kotlin}")
        classpath("com.likethesalad.android:string-reference:${Versions.stringReference}")
        classpath("com.google.protobuf:protobuf-gradle-plugin:${Versions.protobufGradle}")
        classpath("name.remal:gradle-plugins:${Versions.checkUpdates}")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
