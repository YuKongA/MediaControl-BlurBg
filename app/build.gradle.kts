import com.android.build.gradle.internal.api.BaseVariantOutputImpl

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "top.yukonga.mediaControlBlur"
    compileSdk = 34

    defaultConfig {
        applicationId = namespace
        minSdk = 34
        targetSdk = 34
        versionCode = 2110
        versionName = "2.1.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions",
            "-language-version=2.0",
        )
    }

    packaging {
        resources {
            excludes += "**"
        }
        applicationVariants.all {
            outputs.all {
                (this as BaseVariantOutputImpl).outputFileName = "MediaControlBlur-$versionName.apk"
            }
        }
    }

    androidResources {
        additionalParameters += arrayOf("--allow-reserved-package-id", "--package-id", "0x62")
    }
}

dependencies {
    compileOnly(libs.xposed)
    implementation(libs.ezXHelper)
}