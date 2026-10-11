import org.gradle.jvm.tasks.Jar

description = "AgentGo user-owned MCP Streamable HTTP server management module"

// The Streamable HTTP client talks to a remote MCP server, and auto-configuration
// only wires Spring beans. Unit tests cover the management API around that boundary.
kover {
    reports {
        filters {
            excludes {
                classes(
                    "com.agentgo.mcp.client.McpStreamableHttpClientImpl*",
                    "com.agentgo.mcp.config.*",
                )
            }
        }
    }
}

tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    api(project(":agentgo-commons"))
    api(project(":agentgo-dto:agentgo-dto-mcp"))
    api(project(":agentgo-app-modules:agentgo-persistence-starter"))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${rootProject.extra["mockitoKotlinVersion"]}")
}
