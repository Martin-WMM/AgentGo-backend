import org.gradle.jvm.tasks.Jar

description = "Core AI workflow components"

tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    implementation(project(":agentgo-dto"))
    implementation("org.bsc.langgraph4j:langgraph4j-core:${rootProject.extra["langgraph4jVersion"]}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${rootProject.extra["jackson2Version"]}")
    implementation("org.slf4j:slf4j-api")
}
