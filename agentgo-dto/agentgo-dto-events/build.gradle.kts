import org.gradle.jvm.tasks.Jar

description = "AgentGo global system-event data transfer objects"

tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    api(project(":agentgo-dto"))
    api("org.springdoc:springdoc-openapi-starter-common:${rootProject.extra["springdocVersion"]}")
}
