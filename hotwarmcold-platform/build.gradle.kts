import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

fun ShadowJar.genericJarConfig(jarName: String, mainClass: String) {
    archiveClassifier.set("all")
    archiveBaseName.set(jarName)
    archiveVersion.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
    manifest {
        attributes("Main-Class" to mainClass)
    }
    val main by kotlin.jvm().compilations
    from(main.output)
    configurations += main.compileDependencyFiles as Configuration
    configurations += main.runtimeDependencyFiles as Configuration
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }

        tasks.register<ShadowJar>("generateJar") {
            genericJarConfig(
                "smartphone",
                "it.nicolasfarabegoli.hotwarmcold.smartphone.BehaviourUnitKt"
            )
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
