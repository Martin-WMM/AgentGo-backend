# Changelog

All notable changes to AgentGo Backend are documented in this file.

## [0.1.0-SNAPSHOT] - Unreleased

### Added

- Added an isolated local Authentik identity-center deployment with PostgreSQL, secret-safe
  environment templates, and OIDC setup guidance.
- Branded the local Authentik default brand as AgentGo using the shared AgentGo Logo asset.
- Fixed the Authentik AgentGo brand asset mount to use the public media directory so the logo
  resolves in the admin interface.
- Added a `local-deployments` Docker Compose stack for PostgreSQL and the executable web service.
- Added Docker build and ignore files, an environment example, and local deployment verification instructions.
- Added the `db-migration` Flyway migration directory and the initial schema metadata migration.
- Added the `agentgo-app-modules` group with composable web, observability, persistence, and AI Spring Boot starters.
- Added the `agentgo-app` Spring Boot web service module.
- Added the `agentgo-cli` Spring Shell command-line module with the `agentgo version` command.
- Added the `agentgo-commons` shared utility module.
- Added the `agentgo-core` AI workflow module with LangGraph4j integration.
- Added the `agentgo-dto` module for cross-module data transfer objects and protocol models.
- Added the `agentgo-springboot-starter` module with shared auto-configuration and foundational beans.
- Added Actuator, Prometheus metrics, OpenTelemetry tracing, Spring AI, Spring Data JPA, PostgreSQL, Springdoc OpenAPI, and Spring Shell integration points.
- Added GitHub Actions for commit review, dependency review, Merge CI, CodeQL, and tagged releases.
- Added repository issue and pull request templates, security policy documentation, and branch protection rules.

### Changed

- Centralized project, Java, Kotlin, Spring Boot, Spring AI, Springdoc, LangGraph4j, Spring Shell, and Kover versions in the root `build.gradle.kts`.
- Standardized the toolchain on Java 25 LTS, Kotlin 2.3.x, and Gradle 9.1+.
- Configured the build to publish exactly two executable Spring Boot Jars: the web application and the CLI.
- Kept commons, core, DTO, and starter modules as internal library modules rather than standalone release artifacts.

### Quality

- Required commit format: `<emoji><type>: <message>`.
- Enforced a changed-line limit of fewer than 300 lines per commit.
- Required tests and at least 85% code coverage in Merge CI.
- Enabled dependency review, secret scanning, security advisories, and code scanning workflows.
