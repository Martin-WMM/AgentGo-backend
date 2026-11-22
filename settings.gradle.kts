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
    ":agentgo-dto:agentgo-dto-mcp",
    ":agentgo-dto:agentgo-dto-a2a",
    ":agentgo-dto:agentgo-dto-prompts",
    ":agentgo-dto:agentgo-dto-settings",
    ":agentgo-dto:agentgo-dto-cli",
    ":agentgo-dto:agentgo-dto-events",
    ":agentgo-app-modules:agentgo-app-models",
    ":agentgo-app-modules:agentgo-app-mcp-servers",
    ":agentgo-app-modules:agentgo-app-a2a",
    ":agentgo-app-modules:agentgo-app-prompts",
    ":agentgo-app-modules:agentgo-app-settings",
    ":agentgo-app-modules:agentgo-app-cli",
    ":agentgo-app-modules:agentgo-app-events",

    ":agentgo-springboot-starter",
    ":agentgo-cli",
)
