plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.pluginCompose)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
    js(IR) {
        outputModuleName = "mafiaApp"
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.navigation.compose)
            implementation(libs.gitlive.firebase.auth)
            implementation(libs.gitlive.firebase.firestore)
        }
    }
}
