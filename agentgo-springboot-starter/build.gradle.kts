description = "AgentGo Spring Boot starter"

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:4.1.1"))
    api(project(":agentgo-commons"))
    api("org.springframework.boot:spring-boot-autoconfigure")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
