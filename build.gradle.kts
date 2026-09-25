import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm") version "2.3.0" apply false
    kotlin("plugin.spring") version "2.3.0" apply false
    id("org.springframework.boot") version "4.1.1" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.1" apply false
}

allprojects {
    group = "com.agentgo"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlinx.kover")

    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(25)
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget("25"))
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    dependencies {
        "testImplementation"(kotlin("test"))
    }

    extensions.configure<KoverProjectExtension> {
        reports {
            verify {
                rule {
                    minBound(85)
                }
            }
        }
    }
}
