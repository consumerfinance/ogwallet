import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinSerialization)
    kotlin("native.cocoapods")
}

fun detectTarget(): String {
    val hostOs = when (val os = System.getProperty("os.name").lowercase()) {
        "mac os x" -> "macos"
        else -> os.split(" ").first()
    }
    val hostArch = when (val arch = System.getProperty("os.arch").lowercase()) {
        "x86_64" -> "amd64"
        "arm64" -> "aarch64"
        else -> arch
    }
    val renderer = when (hostOs) {
        "macos" -> "metal"
        else -> "opengl"
    }
    return "${hostOs}-${hostArch}-${renderer}"
}



kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    jvm()
    
    js {
        browser()
        binaries.executable()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    
    sourceSets {
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
            implementation(libs.sqliter.driver)
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.biometric)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.sqlcipher.android) // This provides SupportOpenHelperFactory
            implementation(libs.koin.android)
            implementation("androidx.work:work-runtime-ktx:2.9.0")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
            implementation(libs.maplibre.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.sqldelight.jdbc.driver)
            implementation("org.xerial:sqlite-jdbc:3.45.0.0")
            implementation("org.maplibre.compose:maplibre-compose:0.12.1")
            runtimeOnly("org.maplibre.compose:maplibre-native-bindings-jni:0.12.1") {
                capabilities {
                    requireCapability("org.maplibre.compose:maplibre-native-bindings-jni-${detectTarget()}")
                }
            }
        }
        wasmJsMain.dependencies {
            implementation(libs.sqldelight.web.driver)
        }
    }

    cocoapods {
        // REQUIRED: The version of your shared module as an iOS Pod
        version = "0.5.0"

        // REQUIRED: Metadata for the generated .podspec
        summary = "Encrypted transaction vault logic for OGWallet"
        homepage = "https://github.com/consumerfinance/ogwallet"

        ios.deploymentTarget = "15.0"

        // Linking SQLCipher
        pod("SQLCipher") {
            version = "~> 4.0" // Version of the ACTUAL SQLCipher library
        }

        pod("MapLibre", "6.17.1")

        podfile = project.file("../iosApp/Podfile")
    }
}

sqldelight {
    databases {
        create("OGVault") {
            packageName.set("dev.ogwallet.db")
            // This is key: Enable SQLCipher for encryption
            dialect(libs.sqldelight.sqlite.dialect)
        }
    }
}

android {
    namespace = "dev.consumerfinance.ogwallet"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.consumerfinance.ogwallet"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.5.0"
    }
    signingConfigs {
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = System.getenv("STORE_PASSWORD") ?: "android"
            keyAlias = System.getenv("KEY_ALIAS") ?: "androiddebugkey"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "android"
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose{
    desktop {
        application {
            mainClass = "dev.consumerfinance.ogwallet.MainKt"

            nativeDistributions {
                targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                packageName = "dev.consumerfinance.ogwallet"
                packageVersion = "1.0.0"
            }
        }
    }


}
