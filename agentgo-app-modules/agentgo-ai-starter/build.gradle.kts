description = "AgentGo AI integration Spring Boot starter"

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    api(platform("org.springframework.ai:spring-ai-bom:${rootProject.extra["springAiVersion"]}"))
    api(project(":agentgo-springboot-starter"))
    api(project(":agentgo-core"))
    api("org.springframework.ai:spring-ai-starter-model-openai")
}
