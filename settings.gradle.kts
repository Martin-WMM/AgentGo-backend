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
    ":agentgo-app-modules:agentgo-web-starter",
    ":agentgo-app-modules:agentgo-observability-starter",
    ":agentgo-app-modules:agentgo-persistence-starter",
    ":agentgo-app-modules:agentgo-ai-starter",
    ":agentgo-commons",
    ":agentgo-core",
    ":agentgo-dto",
    ":agentgo-springboot-starter",
    ":agentgo-cli",
)
