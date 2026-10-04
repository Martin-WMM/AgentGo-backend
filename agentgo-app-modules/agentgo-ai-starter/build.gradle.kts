description = "AgentGo core workflow Spring Boot starter"

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    api(project(":agentgo-springboot-starter"))
    api(project(":agentgo-core"))
}
