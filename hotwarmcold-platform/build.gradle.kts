plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    val commonMain by sourceSets.getting {
        dependencies {
            implementation(libs.coroutine.core)
            implementation(libs.pulverization.core)
            implementation(libs.pulverization.platform)
            implementation(libs.pulverization.rabbitmq)
            implementation(libs.koin.core)
            implementation(libs.serialization.json)
            implementation(project(":common"))
        }
    }
}
