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
- `db-migration`: Versioned Flyway SQL migrations loaded by `agentgo-app` at startup.

Keep dependencies flowing toward shared modules. `agentgo-app` should compose capabilities
through `agentgo-app-modules` rather than declaring their infrastructure dependencies directly.
Avoid introducing application-specific dependencies into `agentgo-dto` or `agentgo-commons`.

## Build and test

The repository's CI uses Gradle 9.1 with JDK 25. The Gradle Wrapper may exist in a local
workspace but is not part of the tracked project contract; use the wrapper when available,
otherwise use Gradle 9.1 or newer:

```bash
./gradlew test koverVerify
./gradlew build

# Or, when the local wrapper is unavailable:
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
and the `agentgo-app` image. Database changes must be made through versioned Flyway migrations
in `db-migration`; keep `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` in all environments.

The local Authentik identity center is defined separately in
`local-deployment/docker-compose.yml`. It owns its own PostgreSQL volume and must not reuse the
AgentGo application database. Keep Authentik credentials, client secrets, SMTP settings, and
generated data in the ignored `.env` and Docker volumes. Do not copy credentials or data from
other deployment repositories.

Start the local stack with:

```bash
cd local-deployments
cp .env.example .env
docker compose up --build -d
```

The local Compose file uses a placeholder `OPENAI_API_KEY` only to allow Spring AI to
initialize. Replace it with a real key before testing model calls. Never commit `.env`.

Verify the local stack with:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/swagger-ui.html
```

Use `docker compose logs -f agentgo-app` when diagnosing startup failures. Stop the stack
with `docker compose down`; use `docker compose down -v` only when removing local database
data is intentional.

## Coding conventions

- Use Kotlin for new application and library code.
- Use constructor injection for Spring dependencies.
- Keep DTOs stable, serialization-friendly, and free of Spring or persistence concerns.
- Use Actuator for health and operational endpoints; do not add a custom health controller.
- Keep application capability dependencies in `agentgo-app-modules`; `agentgo-app` should
  compose those starters rather than re-declaring their infrastructure dependencies.
- Add database changes only as new Flyway migrations under
  `db-migration/src/main/resources/db/migration/` using `V<version>__<description>.sql`.
- Never edit a migration that has been applied to a shared environment; create a new version.
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
