# AgentGo Backend Contribution Guide

This file defines the repository-specific instructions for working on AgentGo Backend.

## Project overview

AgentGo Backend is a Kotlin, Gradle, Spring MVC, and Spring Boot 4 multi-module project.
The project uses Java 25 LTS, Kotlin 2.3.x, and Gradle 9.1+.

All project and dependency versions are managed in the root `build.gradle.kts`. Do not add
module-local version literals when a version is already defined there.

## Module responsibilities

- `agentgo-app`: Executable Spring Boot web service. It owns HTTP endpoints, Actuator,
  Springdoc OpenAPI, persistence integration, metrics, tracing, and application wiring.
- `agentgo-app-modules`: Group of composable application capability starters consumed by
  `agentgo-app`.
- `agentgo-app-modules:agentgo-web-starter`: Web MVC and Springdoc OpenAPI capabilities.
- `agentgo-app-modules:agentgo-observability-starter`: Actuator, metrics, and tracing capabilities.
- `agentgo-app-modules:agentgo-persistence-starter`: JPA and PostgreSQL capabilities.
- `agentgo-app-modules:agentgo-ai-starter`: Spring AI and AgentGo core workflow capabilities.
- `agentgo-cli`: Executable Spring Shell command-line application for the `agentgo ...`
  command family.
- `agentgo-commons`: Shared utility types and helpers. Keep transport DTOs out of this module.
- `agentgo-core`: AI workflow and orchestration components, including LangGraph4j integration.
- `agentgo-dto`: Cross-module data transfer objects and protocol models. DTOs shared between
  modules belong here.
- `agentgo-springboot-starter`: Reusable Spring Boot auto-configuration, foundational beans,
  and shared infrastructure configuration.

Keep dependencies flowing toward shared modules. `agentgo-app` should compose capabilities
through `agentgo-app-modules` rather than declaring their infrastructure dependencies directly.
Avoid introducing application-specific dependencies into `agentgo-dto` or `agentgo-commons`.

## Build and test

The repository currently relies on the Gradle executable rather than a checked-in Gradle
Wrapper. Use Gradle 9.1 or newer with JDK 25:

```bash
gradle test koverVerify
gradle build
```

The build must maintain at least 85% code coverage. Every module test must use the JUnit
Platform and should cover new behavior and public protocol changes.

The build publishes exactly two executable Spring Boot Jars:

- `agentgo-app/build/libs/agentgo-app-*.jar`
- `agentgo-cli/build/libs/agentgo-cli-*.jar`

The commons, core, DTO, and starter modules are internal library modules and must not be
published as standalone release artifacts.

For local container testing, use `local-deployments/docker-compose.yml`. It starts PostgreSQL
and the `agentgo-app` image. The local Compose environment may use `SPRING_JPA_HIBERNATE_DDL_AUTO=update`
until database migration scripts are introduced; do not change the default application setting
of `validate` for production-like environments.

## Coding conventions

- Use Kotlin for new application and library code.
- Use constructor injection for Spring dependencies.
- Keep DTOs stable, serialization-friendly, and free of Spring or persistence concerns.
- Use Actuator for health and operational endpoints; do not add a custom health controller.
- Document externally visible API changes in `README.md` and `CHANGELOG.md` when appropriate.
- Never commit secrets, credentials, tokens, private keys, or production data.
- Keep changes focused and preserve unrelated user work.

## Version and dependency changes

When adding or upgrading a dependency:

1. Define or update its version in the root `build.gradle.kts`.
2. Use versionless dependency declarations in module build files when dependency management
   provides the version.
3. Update documentation or `CHANGELOG.md` if the change affects users or release artifacts.
4. Run tests, coverage verification, and the full build.

## Git and pull request workflow

The intended flow is:

```text
main -> release/* -> feature/* or fix/* -> PR -> release/* -> PR -> main
```

- `main` and `release/*` are protected and must not receive direct commits.
- Pull requests must pass Merge CI, Commit Review, Dependency Review, and Review Policy.
- Pull requests created by the repository owner may pass Review Policy automatically.
- Pull requests created by other contributors require an approval from a reviewer other than
  the author.
- Use the repository PR template and complete every applicable section.
- Use the issue templates for bug reports and feature requests.
- Keep the PR description in English and document release impact and validation results.

Commit messages must use the format:

```text
<emoji><type>: <message>
```

Each commit must change fewer than 300 lines. Examples:

```text
✨feat: add agent workflow endpoint
🐛fix: handle missing request identifier
📝docs: update release instructions
```

## Security and quality

Security-sensitive changes require extra care and must not weaken repository protections.
The repository uses security advisories, secret scanning, dependency review, CodeQL, and
private vulnerability reporting. Report vulnerabilities through the repository security
advisory process rather than opening a public issue.

Before handoff, verify:

- `git diff --check` passes.
- Tests and `koverVerify` pass.
- `gradle build` succeeds.
- Only the two intended executable Jars are release artifacts.
- Documentation and changelog entries are updated when required.
- The working tree contains only intentional changes.
