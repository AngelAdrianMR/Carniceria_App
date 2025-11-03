plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization")
}


kotlin {
    androidTarget()

    // Targets iOS
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // Nueva jerarquía → evita duplicados de iosMain
    applyDefaultHierarchyTemplate()

    jvmToolchain(17)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:3.1.1")
                implementation("io.ktor:ktor-client-content-negotiation:3.1.1")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("io.github.jan-tennert.supabase:postgrest-kt:3.1.1")
                implementation("io.github.jan-tennert.supabase:auth-kt:3.1.1")       // ✅ para login/email/google
                implementation("io.github.jan-tennert.supabase:functions-kt:3.1.1")
                implementation("io.github.jan-tennert.supabase:storage-kt:3.1.1")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:3.1.1")
            }
        }
        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.1.1")
            }
        }
    }
}

android {
    namespace = "com.carniceria.shared.shared.models.utils"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
