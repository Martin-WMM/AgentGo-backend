plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.1.1"))
    implementation(platform("org.springframework.shell:spring-shell-dependencies:4.0.3"))
    implementation(project(":agentgo-commons"))
    implementation(project(":agentgo-core"))
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
