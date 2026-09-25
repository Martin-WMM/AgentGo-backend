plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.1.1"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation(kotlin("reflect"))
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:4.1.1"))
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
}

springBoot {
    buildInfo()
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "com.agentgo.app.AgentGoApplication",
                    "com.agentgo.app.AgentGoApplicationKt",
                )
            }
        }
    }
}
