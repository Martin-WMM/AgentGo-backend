import org.gradle.jvm.tasks.Jar

description = "Cross-module data transfer objects and protocol models"

// The auth starter consumes DTO classes from its project classpath during the
// application image build, so keep the internal DTO artifact available.
tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    api("org.springdoc:springdoc-openapi-starter-common:${rootProject.extra["springdocVersion"]}")
}
