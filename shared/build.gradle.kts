import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

/*
 * Gemeinsamer Code für Android und iOS:
 * Ergebnislogik, Daten (Room/DataStore), ViewModels, komplette Compose-Oberfläche,
 * Texte/Schriften (Compose-Ressourcen) und die plattformneutrale Monetarisierungslogik.
 * Plattformspezifisches (Play Billing / StoreKit, AdMob-SDKs, Teilen) wird über
 * Schnittstellen aus :app (Android) bzw. iosApp (Swift) eingesteckt.
 */
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    androidLibrary {
        namespace = "dev.flondy.checkyourlove.shared"
        compileSdk = 36
        minSdk = 24
        androidResources { enable = true }
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
    }

    iosArm64()
    iosSimulatorArm64()

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.cmp.runtime)
            implementation(libs.cmp.foundation)
            api(libs.cmp.ui)
            api(libs.cmp.resources)
            api(libs.cmp.material3)
            implementation(libs.cmp.icons.extended)
            api(libs.jb.lifecycle.viewmodel.compose)
            implementation(libs.jb.lifecycle.runtime.compose)
            implementation(libs.jb.lifecycle.savedstate)
            implementation(libs.jb.navigation.compose)
            api(libs.androidx.room.runtime)
            api(libs.androidx.datastore.preferences.core)
            implementation(libs.okio)
            api(libs.kotlinx.coroutines.core)
        }
        iosMain.dependencies {
            // Android nutzt den System-SQLite-Treiber; iOS bringt SQLite gebündelt mit.
            implementation(libs.androidx.sqlite.bundled)
        }
        androidMain.dependencies {
            implementation(libs.androidx.sqlite.framework)
            implementation(libs.kotlinx.coroutines.android)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "dev.flondy.checkyourlove.resources"
    generateResClass = always
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
