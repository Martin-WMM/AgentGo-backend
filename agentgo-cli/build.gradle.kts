plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    implementation(platform("org.springframework.shell:spring-shell-dependencies:${rootProject.extra["springShellVersion"]}"))
    implementation(project(":agentgo-commons"))
    implementation(project(":agentgo-core"))
    implementation(project(":agentgo-dto"))
    implementation(project(":agentgo-springboot-starter"))
    implementation("org.springframework.shell:spring-shell-starter")
    testImplementation("org.springframework.shell:spring-shell-starter-test")
}

kover {
    reports {
        filters {
            excludes {
                classes("com.agentgo.cli.AgentGoCliApplication", "com.agentgo.cli.AgentGoCliApplicationKt")
            }
        }
    }
}
