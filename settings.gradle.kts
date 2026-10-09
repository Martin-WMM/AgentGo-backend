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
    ":agentgo-app-modules:agentgo-app-auth",
    ":agentgo-app-modules:agentgo-app-file",
    ":agentgo-commons",
    ":agentgo-core",
    ":agentgo-dto",
    ":agentgo-dto:agentgo-dto-models",
    ":agentgo-app-modules:agentgo-app-models",
    ":agentgo-springboot-starter",
    ":agentgo-cli",
)
