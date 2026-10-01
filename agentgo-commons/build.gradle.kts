import org.gradle.jvm.tasks.Jar

description = "Shared utility types"

tasks.named<Jar>("jar") {
    enabled = true
}
