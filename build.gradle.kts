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

extra["projectVersion"] = "0.1.0-SNAPSHOT"
extra["javaVersion"] = 25
extra["springBootVersion"] = "4.1.1"
extra["springAiVersion"] = "2.0.1"
extra["springdocVersion"] = "3.1.1"
extra["langgraph4jVersion"] = "1.9.1"
extra["springShellVersion"] = "4.0.3"

allprojects {
    group = "com.agentgo"
    version = rootProject.extra["projectVersion"] as String
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlinx.kover")

    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(rootProject.extra["javaVersion"] as Int)
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
