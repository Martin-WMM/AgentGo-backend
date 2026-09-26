description = "Core AI workflow components"

dependencies {
    implementation(project(":agentgo-dto"))
    implementation("org.bsc.langgraph4j:langgraph4j-core:${rootProject.extra["langgraph4jVersion"]}")
}
