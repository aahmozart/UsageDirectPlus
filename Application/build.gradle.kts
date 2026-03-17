plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    compileSdk = 34

    namespace = "godau.fynn.usagedirectplus"

    defaultConfig {
        applicationId = "godau.fynn.usagedirectplus"
        minSdk = 26
        targetSdk = 34
        versionCode = 10
        versionName = "0.8.1"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    flavorDimensions += "source"
    productFlavors {
        create("system") {
            dimension = "source"
            applicationIdSuffix = ".system"
        }
        create("database") {
            dimension = "source"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.palette)

    implementation(libs.smarttablayout) {
        artifact {
            type = "aar"
        }
    }
    implementation(libs.prettytime)
    implementation(libs.pikolo)

    implementation(libs.typedRecyclerView)
    implementation(libs.librariesDirect)
    implementation(libs.chartDirect)

    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.documentfile)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)
    implementation(libs.coroutines.android)
    implementation(libs.appcompat)
    implementation(libs.activity.ktx)
    implementation(libs.material)

    // JVM unit tests
    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.junit5.params)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)

    // Instrumented tests
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.room.testing)
}
