import org.gradle.jvm.tasks.Jar

description = "AgentGo core workflow Spring Boot starter"

tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    api(project(":agentgo-springboot-starter"))
    api(project(":agentgo-core"))
    api(project(":agentgo-dto:agentgo-dto-agent"))
    implementation(project(":agentgo-app-modules:agentgo-app-cli"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.bsc.langgraph4j:langgraph4j-postgres-saver:${rootProject.extra["langgraph4jVersion"]}")
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    testImplementation("org.mockito.kotlin:mockito-kotlin:${rootProject.extra["mockitoKotlinVersion"]}")
    testImplementation("org.springframework.security:spring-security-test")
}
