import org.gradle.jvm.tasks.Jar

description = "AgentGo Authentik OAuth2/OIDC application module"

// The root build disables library JARs by default. This starter must be packaged
// into agentgo-app's bootJar so its auto-configuration and resources are available
// at runtime.
tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    implementation(project(":agentgo-dto"))
    implementation(project(":agentgo-app-modules:agentgo-app-file"))
    api(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-oauth2-client")
    api("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springdoc:springdoc-openapi-starter-common:${rootProject.extra["springdocVersion"]}")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
