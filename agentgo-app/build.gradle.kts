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
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
}

springBoot {
    buildInfo()
}

kover {
    reports {
        filters {
            excludes {
                classes("com.agentgo.app.AgentGoApplication", "com.agentgo.app.AgentGoApplicationKt")
            }
        }
    }
}
