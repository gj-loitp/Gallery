import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mckimquyen.gallery"
        minSdk = 23
        targetSdk = 34
        versionName = "2024.05.09"
        versionCode = 20240509
        setProperty("archivesBaseName", "gallery-$versionCode")
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            register("release") {
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
            }
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    flavorDimensions.add("licensing")
    productFlavors {
        register("foss")
        register("prepaid")
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }

    compileOptions {
        val currentJavaVersionFromLibs = JavaVersion.valueOf(libs.versions.app.build.javaVersion.get())
        sourceCompatibility = currentJavaVersionFromLibs
        targetCompatibility = currentJavaVersionFromLibs
    }

    dependenciesInfo {
        includeInApk = false
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    namespace = "com.mckimquyen.gallery"

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    packaging {
        resources {
            excludes += "META-INF/library_release.kotlin_module"
        }
    }
}

dependencies {
    api(libs.fossify.commons)
    api(libs.android.image.cropper)
    api(libs.exif)
    api(libs.android.gif.drawable)
    api(libs.androidx.constraintlayout)
    api(libs.androidx.media3.exoplayer)
    api(libs.sanselan)
    api(libs.imagefilters)
    api(libs.androidsvg.aar)
    api(libs.gestureviews)
    api(libs.subsamplingscaleimageview)
    api(libs.androidx.swiperefreshlayout)
    api(libs.awebp)
    api(libs.apng)
    api(libs.avif.integration)
    api(libs.okio)
    api(libs.picasso) {
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }
    compileOnly(libs.okhttp)
    ksp(libs.glide.compiler)
    api(libs.zjupure.webpdecoder)
    api(libs.bundles.room)
    ksp(libs.androidx.room.compiler)
}
