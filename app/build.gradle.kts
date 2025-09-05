import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val abiList = listOf("arm64-v8a", "x86_64")

android {
    namespace = "com.algebnaly.neonfiles"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.algebnaly.neonfiles"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += abiList
        }
        ndkVersion = "27.3.13750724"
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
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.coil)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}


val nfs4cLibProjectPath = "../../../rust/nfscrs_jni/"
val jniStorePath = layout.projectDirectory.dir("src/main/jniLibs")
val ndkPath = android.ndkDirectory.absolutePath


tasks.named("preBuild") {
    dependsOn(buildNFSCrsLib)
}

val buildNFSCrsLib by tasks.registering(Exec::class) {
    inputs.dir("$nfs4cLibProjectPath/src")
    outputs.dir(jniStorePath)
    workingDir = File(nfs4cLibProjectPath)
    commandLine = listOf(
        "cargo", "ndk"
    ) + abiList.flatMap { listOf("-t", it) } + listOf(
        "-o", "$projectDir/src/main/jniLibs",
        "build", "--release"
    )
    environment("ANDROID_NDK_HOME", ndkPath)
}