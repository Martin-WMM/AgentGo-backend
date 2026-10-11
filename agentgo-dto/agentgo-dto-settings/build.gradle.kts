import org.gradle.jvm.tasks.Jar

description = "AgentGo user system-settings data transfer objects"

tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    api("org.springdoc:springdoc-openapi-starter-common:${rootProject.extra["springdocVersion"]}")
}
