import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.com.google.dagger.hilt.android)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "de.langerhans.odintools"
    compileSdk = 34

    defaultConfig {
        applicationId = "de.langerhans.odintools"
        minSdk = 33
        targetSdk = 34
        versionCode = 11
        versionName = "1.3.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            initWith(buildTypes.getByName("debug"))
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            applicationVariants.all {
                val variant = this
                outputs
                    .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                    .forEach {
                        it.outputFileName = "OdinTools-${variant.versionName}.apk"
                    }
            }
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    hilt {
        enableAggregatingTask = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    jvmToolchain(11)
}

ktlint {
    reporters {
        reporter(ReporterType.SARIF)
    }
    relative.set(true)
}

dependencies {
    // Compose BOM specifics
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    debugImplementation(composeBom)

    // Normal imports
    implementation(libs.bundles.app)
    debugImplementation(libs.bundles.appDebug)
    annotationProcessor(libs.bundles.appAnnotationProcessor)
    ksp(libs.bundles.appKsp)
    testImplementation(libs.bundles.appUnitTest)
    androidTestImplementation(libs.bundles.appAndroidTest)

    // Hilt dependencies
    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.android.compiler)
}
