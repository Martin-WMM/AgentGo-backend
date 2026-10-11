import org.gradle.jvm.tasks.Jar

description = "AgentGo core workflow Spring Boot starter"

tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    api(project(":agentgo-springboot-starter"))
    api(project(":agentgo-core"))
}
