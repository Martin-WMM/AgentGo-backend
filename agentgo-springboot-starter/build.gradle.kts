description = "AgentGo Spring Boot starter"

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    api(project(":agentgo-commons"))
    api(project(":agentgo-dto"))
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.boot:spring-boot-starter-aspectj")
    api("org.springframework.boot:spring-boot-starter-json")
    api("org.springdoc:springdoc-openapi-starter-common:${rootProject.extra["springdocVersion"]}")
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui:${rootProject.extra["springdocVersion"]}")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
