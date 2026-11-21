import org.gradle.jvm.tasks.Jar

description = "AgentGo Spring Boot starter"

// Downstream modules and the IDE resolve this project as a JAR on the classpath.
tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    api(project(":agentgo-commons"))
    api(project(":agentgo-dto"))
    api("jakarta.servlet:jakarta.servlet-api")
    api("com.fasterxml.jackson.core:jackson-databind:${rootProject.extra["jackson2Version"]}")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${rootProject.extra["jackson2Version"]}")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:${rootProject.extra["jackson2Version"]}")
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.boot:spring-boot-starter-aspectj")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-json")
    api("org.springframework:spring-webmvc")
    api("org.springdoc:springdoc-openapi-starter-common:${rootProject.extra["springdocVersion"]}")
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui:${rootProject.extra["springdocVersion"]}")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
