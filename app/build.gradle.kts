plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
    id("org.jmailen.kotlinter")
}

android {
    compileSdkVersion(Configuration.compileSdk)
    buildToolsVersion(Configuration.buildTools)

    defaultConfig {
        applicationId = "de.datlag.openfe"
        minSdkVersion(Configuration.minSdk)
        targetSdkVersion(Configuration.targetSdk)
        versionCode = Configuration.versionCode
        versionName = Configuration.versionName

        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
            isShrinkResources = false
            isZipAlignEnabled = false
            isJniDebuggable = true
            isRenderscriptDebuggable = true
        }

        getByName("release") {
            isMinifyEnabled = true
            isDebuggable = false
            isShrinkResources = true
            isZipAlignEnabled = true
            isJniDebuggable = false
            isRenderscriptDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = CompileOptions.sourceCompatibility
        targetCompatibility = CompileOptions.targetCompatibility
    }

    kotlinOptions {
        jvmTarget = CompileOptions.jvmTarget
    }

    aaptOptions {
        additionalParameters = listOf("--no-version-vectors")
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib${CompileOptions.kotlinJdk}:${Versions.kotlin}")

    testImplementation("junit:junit:${Versions.junit}")
    androidTestImplementation("androidx.test.ext:junit:${Versions.testJunit}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Versions.testEspresso}")

    implementation("androidx.multidex:multidex:${Versions.multidex}")
    implementation("androidx.appcompat:appcompat:${Versions.appcompat}")
    implementation("androidx.legacy:legacy-support-v4:${Versions.legacySupportV4}")
    implementation("com.google.android.material:material:${Versions.material}")
    implementation("com.jakewharton.timber:timber:${Versions.timber}")

    implementation("androidx.core:core-ktx:${Versions.ktxCore}")
    implementation("androidx.activity:activity-ktx:${Versions.ktxActivity}")
    implementation("androidx.fragment:fragment-ktx:${Versions.ktxFragment}")
    implementation("androidx.collection:collection-ktx:${Versions.ktxCollection}")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}")

    implementation("androidx.coordinatorlayout:coordinatorlayout:${Versions.coordinatorlayout}")
    implementation("androidx.constraintlayout:constraintlayout:${Versions.constraintlayout}")
    implementation("androidx.recyclerview:recyclerview:${Versions.recyclerview}")
    implementation("androidx.cardview:cardview:${Versions.cardview}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")

    implementation("androidx.navigation:navigation-runtime-ktx:${Versions.navigation}")
    implementation("androidx.navigation:navigation-fragment-ktx:${Versions.navigation}")
    implementation("androidx.navigation:navigation-ui-ktx:${Versions.navigation}")

    implementation("androidx.hilt:hilt-lifecycle-viewmodel:${Versions.hiltAndroidX}")
    implementation("com.google.dagger:hilt-android:${Versions.hilt}")
    kapt("com.google.dagger:hilt-android-compiler:${Versions.hilt}")
    kapt("androidx.hilt:hilt-compiler:${Versions.hiltAndroidX}")

    implementation("io.github.inflationx:viewpump:${Versions.viewpump}")
    implementation("io.github.inflationx:calligraphy3:${Versions.calligraphy}")
    implementation("com.mikhaellopez:circularprogressbar:${Versions.circularprogress}")
    implementation("com.github.Ferfalk:SimpleSearchView:${Versions.searchview}")

    implementation("com.karumi:dexter:${Versions.dexter}")
    implementation("com.github.bumptech.glide:glide:${Versions.glide}")
    kapt("com.github.bumptech.glide:compiler:${Versions.glide}")

    implementation("me.jahnen:libaums:${Versions.libaums}")
}
