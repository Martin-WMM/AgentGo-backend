description = "AgentGo Spring Boot starter"

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    api(project(":agentgo-commons"))
    api(project(":agentgo-dto"))
    api("org.springframework.boot:spring-boot-autoconfigure")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
