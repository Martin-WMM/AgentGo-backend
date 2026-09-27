import org.gradle.jvm.tasks.Jar

description = "AgentGo file metadata and MinIO object storage module"

tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    api(project(":agentgo-commons"))
    api(project(":agentgo-dto"))
    api(project(":agentgo-app-modules:agentgo-persistence-starter"))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.minio:minio:${rootProject.extra["minioVersion"]}")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
