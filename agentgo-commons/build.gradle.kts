import org.gradle.jvm.tasks.Jar

description = "Shared utility types"

tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    api("org.springdoc:springdoc-openapi-starter-common:${rootProject.extra["springdocVersion"]}")
}
