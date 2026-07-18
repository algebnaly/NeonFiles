import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
}

val supportedAbiList = listOf("arm64-v8a", "x86_64")

val keystorePropertiesFile = rootProject.file("keystore.properties")

val keystoreProperties = Properties().apply {
    require(keystorePropertiesFile.isFile) {
        "Missing signing configuration: ${keystorePropertiesFile.absolutePath}"
    }

    keystorePropertiesFile.inputStream().use {
        load(it)
    }
}

android {
    namespace = "com.algebnaly.neonfiles"
    //noinspection GradleDependency
    compileSdk = 36
    ndkVersion = "27.3.13750724"

    defaultConfig {
        applicationId = "com.algebnaly.neonfiles"
        minSdk = 33
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += supportedAbiList
        }
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(
                keystoreProperties.getProperty("storeFile")
            )
            storePassword = keystoreProperties.getProperty("storePassword")
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")

            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        register("staging") {
            initWith(getByName("release"))
            isDebuggable = false
            isJniDebuggable = false
            isMinifyEnabled = false
            versionNameSuffix = "-STAGING"
            signingConfig = signingConfigs.getByName("debug")
        }

        register("prebuilt") {
            initWith(getByName("release"))
            // Use the checked-in libraries from src/main/jniLibs instead of
            // rebuilding the native project, which may be temporarily broken.
            matchingFallbacks += listOf("release")
            versionNameSuffix = "-PREBUILT"
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

val nfscrsJniLibProjectPath = "../../../rust/nfscrs_jni/"
val ndkPath: String = android.ndkDirectory.absolutePath

androidComponents {
    onVariants { variant ->
        if (variant.buildType in listOf("debug", "release", "staging")) {
            val taskName = "buildRustJni${variant.name.replaceFirstChar { it.uppercase() }}"
            val buildRustJni = tasks.register<BuildRustJniTask>(taskName) {
                description = "build nfscrs native lib"
                jniCrateSrc.set(layout.projectDirectory.dir(nfscrsJniLibProjectPath).dir("src"))
                abiList.set(supportedAbiList)
                ndkHome.set(ndkPath)
                rustFlags.set("")
                outputDirectory.set(layout.buildDirectory.dir("generated/rustJni/${variant.name}"))
            }
            variant.sources.jniLibs?.addGeneratedSourceDirectory(
                buildRustJni,
                BuildRustJniTask::outputDirectory
            )
        } else if (variant.buildType == "prebuilt") {
            variant.sources.jniLibs?.addStaticSourceDirectory("src/main/prebuiltLibs")
        }
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
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}