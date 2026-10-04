import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.roborazzi)
}

// ---------------------------------------------------------------------------
// Monetarisierungs-Konfiguration: monetization.properties, überschreibbar per -P
// ---------------------------------------------------------------------------
val monetizationFile = rootProject.file("monetization.properties")
val monetization = Properties().apply {
    if (monetizationFile.exists()) monetizationFile.inputStream().use { load(it) }
}

fun cfg(key: String): String =
    (providers.gradleProperty(key).orNull ?: monetization.getProperty(key) ?: "").trim()

fun String.asBuildConfigString(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

// Offizielle Google-Test-IDs (https://developers.google.com/admob/android/test-ads)
val testAdmobAppId = "ca-app-pub-3940256099942544~3347511713"
val testBannerId = "ca-app-pub-3940256099942544/9214589741"
val testInterstitialId = "ca-app-pub-3940256099942544/1033173712"
val testRewardedId = "ca-app-pub-3940256099942544/5224354917"
val testRewardedInterstitialId = "ca-app-pub-3940256099942544/5354046379"
// Format des Prämien-Anzeigenblocks: "rewarded_interstitial" (Interstitial mit Prämie) oder "rewarded"
val rewardedIsInterstitial = cfg("cyl.admobRewardedFormat").ifBlank { "rewarded" } == "rewarded_interstitial"

val releaseAdmobAppId = cfg("cyl.admobAppId")

android {
    namespace = "dev.flondy.checkyourlove"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.flondy.checkyourlove"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "PREMIUM_PRODUCT_ID", cfg("cyl.premiumProductId").asBuildConfigString())
        buildConfigField("String", "PLAY_LICENSE_KEY", cfg("cyl.playLicenseKey").asBuildConfigString())
        buildConfigField("String", "PRIVACY_POLICY_URL", cfg("cyl.privacyPolicyUrl").asBuildConfigString())
        buildConfigField("boolean", "AD_REWARDED_IS_INTERSTITIAL", rewardedIsInterstitial.toString())
    }

    buildTypes {
        debug {
            // Debug: ausschließlich Testanzeigen
            manifestPlaceholders["admobAppId"] = testAdmobAppId
            buildConfigField("String", "AD_BANNER_ID", testBannerId.asBuildConfigString())
            buildConfigField("String", "AD_INTERSTITIAL_ID", testInterstitialId.asBuildConfigString())
            buildConfigField(
                "String", "AD_REWARDED_ID",
                (if (rewardedIsInterstitial) testRewardedInterstitialId else testRewardedId).asBuildConfigString(),
            )
            // Simulierte Käufe nur im Debug-Build und nur, solange keine echte Produkt-ID gesetzt ist
            buildConfigField("boolean", "ALLOW_SIMULATED_BILLING", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            manifestPlaceholders["admobAppId"] = releaseAdmobAppId
            buildConfigField("String", "AD_BANNER_ID", cfg("cyl.admobBannerId").asBuildConfigString())
            buildConfigField("String", "AD_INTERSTITIAL_ID", cfg("cyl.admobInterstitialId").asBuildConfigString())
            buildConfigField("String", "AD_REWARDED_ID", cfg("cyl.admobRewardedId").asBuildConfigString())
            buildConfigField("boolean", "ALLOW_SIMULATED_BILLING", "false")
            // Keine Signierungskonfiguration hier: Release-Signierung erfolgt wie bisher
            // über Android Studio (Build > Generate Signed App Bundle) bzw. Play App Signing.
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { it.systemProperty("robolectric.pixelCopyRenderMode", "hardware") }
        }
    }

    packaging {
        resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}", "/META-INF/LICENSE*")
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        warningsAsErrors = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

// Release-Builds ohne AdMob-App-ID wären nicht lauffähig (das Werbe-SDK bricht beim Start ab).
// Statt eine ID zu erfinden, schlägt der Release-Build mit einer klaren Anleitung fehl.
gradle.taskGraph.whenReady {
    val buildsRelease = allTasks.any { it.project == project && it.name.contains("Release") &&
        (it.name.startsWith("assemble") || it.name.startsWith("bundle") || it.name.startsWith("package")) }
    if (buildsRelease && releaseAdmobAppId.isBlank()) {
        throw GradleException(
            "Release-Build: cyl.admobAppId ist nicht gesetzt. Trage die AdMob-App-ID in " +
                "monetization.properties ein (siehe docs/MONETARISIERUNG.md)."
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.billing.ktx)
    implementation(libs.play.services.ads)
    implementation(libs.ump)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.room.testing)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
}
