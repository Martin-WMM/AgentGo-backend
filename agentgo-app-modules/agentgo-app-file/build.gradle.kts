import org.gradle.jvm.tasks.Jar

description = "AgentGo file metadata and MinIO object storage module"

// MinIO and multipart handling are integration-boundary code. The application
// tests exercise the deployed storage path; exclude adapters from unit coverage.
kover {
    reports {
        filters {
            excludes {
                classes("com.agentgo.file.**")
            }
        }
    }
}

tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    api(project(":agentgo-commons"))
    api(project(":agentgo-dto"))
    api(project(":agentgo-app-modules:agentgo-persistence-starter"))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.minio:minio:${rootProject.extra["minioVersion"]}")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:${rootProject.extra["springBootVersion"]}"))
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${rootProject.extra["mockitoKotlinVersion"]}")
    testRuntimeOnly("com.h2database:h2")
}
