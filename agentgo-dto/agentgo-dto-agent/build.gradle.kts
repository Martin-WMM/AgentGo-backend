import org.gradle.jvm.tasks.Jar

description = "AgentGo agent stream data transfer objects"

tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    api(project(":agentgo-commons"))
    api("org.springdoc:springdoc-openapi-starter-common:${rootProject.extra["springdocVersion"]}")
    api("jakarta.validation:jakarta.validation-api")
}
