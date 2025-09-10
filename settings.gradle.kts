pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.6.0"
        id("com.android.library") version "8.6.0"
        id("org.jetbrains.kotlin.android") version "2.2.0"
        id("org.jetbrains.kotlin.multiplatform") version "2.2.0"
        id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
        id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Carniceria_App"
include(":app")
include(":shared")
