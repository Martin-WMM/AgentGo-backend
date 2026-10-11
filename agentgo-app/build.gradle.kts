plugins {
    id("org.springframework.boot")
}

sourceSets {
    main {
        resources.srcDir(layout.projectDirectory.dir("../db-migration/src/main/resources"))
    }
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    implementation(project(":agentgo-springboot-starter"))
    implementation(project(":agentgo-app-modules:agentgo-web-starter"))
    implementation(project(":agentgo-app-modules:agentgo-observability-starter"))
    implementation(project(":agentgo-app-modules:agentgo-persistence-starter"))
    implementation(project(":agentgo-app-modules:agentgo-ai-starter"))
    implementation(kotlin("reflect"))
    implementation(project(":agentgo-app-modules:agentgo-app-auth"))
    implementation(project(":agentgo-app-modules:agentgo-app-file"))
    implementation(project(":agentgo-app-modules:agentgo-app-models"))
    implementation(project(":agentgo-app-modules:agentgo-app-mcp-servers"))
    implementation(project(":agentgo-app-modules:agentgo-app-prompts"))
    implementation(project(":agentgo-app-modules:agentgo-app-settings"))

    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
}

springBoot {
    buildInfo()
}

/**
 * Loads local-deployment/.bootRun.env for host IDE / Gradle bootRun (dev).
 * Compose "local" profile must not inherit these variables from the shell.
 */
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    val envFile = rootProject.file("local-deployment/.bootRun.env")
    if (envFile.exists()) {
        envFile.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
            .forEach { line ->
                val idx = line.indexOf('=')
                environment(line.substring(0, idx), line.substring(idx + 1))
            }
    }
}

kover {
    reports {
        filters {
            excludes {
                classes("com.agentgo.app.AgentGoApplicationKt")
            }
        }
    }
}
