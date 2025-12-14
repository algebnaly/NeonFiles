import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
}

val abiList = listOf("arm64-v8a", "x86_64")

android {
    namespace = "com.algebnaly.neonfiles"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.algebnaly.neonfiles"
        minSdk = 33
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

        register("staging"){
            initWith(getByName("release"))
            isDebuggable = false
            isJniDebuggable = false
            isMinifyEnabled = false
            versionNameSuffix = "-STAGING"
            signingConfig = signingConfigs.getByName("debug")
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
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.coil)
    implementation(libs.coil.video)
    implementation(libs.nfs4c)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}


val nfscfsJniLibProjectPath = "../../../rust/nfscrs_jni/"
val nfscrsProjectPath = "../../../rust/nfscrs/"
val jniStorePath = layout.projectDirectory.dir("src/main/jniLibs")
val ndkPath: String = android.ndkDirectory.absolutePath

val rustFlags = "-Clink-arg=-Wl,-z,max-page-size=0x4000"


tasks.named("preBuild") {
    dependsOn(buildNFSCrsLib)
}

val buildNFSCrsLib by tasks.registering(Exec::class) {
    inputs.dir("$nfscfsJniLibProjectPath/src")
    inputs.dir("$nfscrsProjectPath/src")
    outputs.dir(jniStorePath)
    workingDir = File(nfscfsJniLibProjectPath)
    commandLine = listOf(
        "cargo", "ndk"
    ) + abiList.flatMap { listOf("-t", it) } + listOf(
        "-o", "$projectDir/src/main/jniLibs",
        "build", "--release",
    )
    environment("ANDROID_NDK_HOME", ndkPath)
    environment("RUSTFLAGS", rustFlags)
}