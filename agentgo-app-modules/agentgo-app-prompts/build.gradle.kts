import org.gradle.jvm.tasks.Jar

description = "AgentGo user-owned prompt template management module"

tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    api(project(":agentgo-commons"))
    api(project(":agentgo-dto:agentgo-dto-prompts"))
    api(project(":agentgo-app-modules:agentgo-persistence-starter"))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.hubspot.jinjava:jinjava:${rootProject.extra["jinjavaVersion"]}")
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${rootProject.extra["mockitoKotlinVersion"]}")
    testRuntimeOnly("com.h2database:h2")
}
