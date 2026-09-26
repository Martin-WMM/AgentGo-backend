import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "agentgo-backend"
include(
    ":agentgo-app",
    ":agentgo-commons",
    ":agentgo-core",
    ":agentgo-dto",
    ":agentgo-springboot-starter",
    ":agentgo-cli",
)
