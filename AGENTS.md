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
- `agentgo-app-modules:agentgo-ai-starter`: AgentGo core workflow capabilities. Spring AI is not wired in yet.
- `agentgo-app-modules:agentgo-app-auth`: Authentik OIDC login, session, and profile capabilities.
- `agentgo-app-modules:agentgo-app-file`: Workspace and avatar file metadata with MinIO storage.
- `agentgo-app-modules:agentgo-app-models`: Authenticated user-owned AI model configuration CRUD.
- `agentgo-app-modules:agentgo-app-mcp-servers`: Authenticated user-owned MCP Streamable HTTP server CRUD and probe.
- `agentgo-app-modules:agentgo-app-settings`: Per-user system settings that configure AgentGo clients.
- `agentgo-cli`: Executable Spring Shell command-line application for the `agentgo ...`
  command family.
- `agentgo-commons`: Shared utility types, helpers, and the standardized HTTP, pagination, and SSE response DTOs.
- `agentgo-core`: AI workflow and orchestration components, including LangGraph4j integration.
- `agentgo-dto`: Cross-module business data transfer objects and protocol models. DTOs shared
  between modules belong here, except for the standardized HTTP, pagination, and SSE response DTOs.
- `agentgo-dto:agentgo-dto-models`: Request and response DTOs for user-owned model management.
- `agentgo-dto:agentgo-dto-mcp`: Request and response DTOs for user-owned MCP Streamable HTTP servers.
- `agentgo-dto:agentgo-dto-settings`: Request and response DTOs for per-user system settings.
- `agentgo-springboot-starter`: Reusable Spring Boot auto-configuration, foundational beans,
  and shared infrastructure configuration.
- `db-migration`: Versioned Flyway SQL migrations loaded by `agentgo-app` at startup.

Keep dependencies flowing toward shared modules. `agentgo-app` should compose capabilities
through `agentgo-app-modules` rather than declaring their infrastructure dependencies directly.
Avoid introducing application-specific dependencies into `agentgo-dto` or `agentgo-commons`.

## Build and test

The repository uses JDK 25 and Gradle 9.1 or newer. Gradle 9.0.0 cannot run on JDK 25; its
maximum Gradle JVM is 24. Do not point the IDE Gradle JVM at Java 24 to work around that.
Keep the checked-in wrapper on a release that can run on JDK 25. The wrapper is currently
Gradle 9.8.1. CI uses this wrapper through `gradle/actions/setup-gradle`.

```bash
./gradlew test koverVerify
./gradlew build

# Or, when the local wrapper is unavailable:
gradle test koverVerify
gradle build
```

The build must maintain at least 85% code coverage. Every module test must use the JUnit
Platform and should cover new behavior and public protocol changes.

### Unit testing (Controller / Service / DAO)

For each capability that exposes HTTP APIs and persistence, write **separate, detailed unit
tests for all three layers**. Do not collapse controller, service, and data-access coverage
into a single mixed test class.

| Layer | Typical type under test | Test style | What to cover |
| --- | --- | --- | --- |
| **Controller** | `XxxController`, exception handlers that map domain errors to HTTP envelopes | Pure unit tests: mock the service (and shared helpers such as `AgentGoMessages`); assert response codes, `responseType`, localized `message`, HTTP status, and that the service is invoked with the expected arguments | Success envelopes for create/get/list/update; delete status (for example `204`); i18n message selection via `LocaleContextHolder`; exception-handler mapping to `CommonHttpResponse` |
| **Service** | `XxxServiceImpl` | Pure unit tests: mock repositories, mappers, and collaborators; authenticate through `SecurityContextHolder` when ownership or principal is required | Happy paths; duplicate/conflict rules; not-found; empty-vs-non-empty delete guards; unauthenticated/unauthorized paths; verify persistence side effects with Mockito (`save` / `delete` called or never called) |
| **DAO / data access** | Spring Data `XxxRepository` (and any dedicated DAO wrappers) | Slice tests such as `@DataJpaTest` with an embedded database (H2 in PostgreSQL mode is acceptable for repository unit tests); import JPA auditing config when entities use audit fields; disable Flyway in the test profile and use `ddl-auto=create-drop` for the slice only | Query methods used by the service (`findBy…`, `existsBy…`, ordering); owner scoping; unique-name checks that ignore the current id; save/audit field population; delete removes the row |

Rules:

- Place tests next to the production package under `src/test/kotlin` (for example
  `…/controller/XxxControllerTest`, `…/service/impl/XxxServiceImplTest`,
  `…/repository/XxxRepositoryTest`).
- Controllers must not be tested by calling repositories; services must not be tested through
  the HTTP stack; repository tests must not mock away the persistence queries under test.
- When a new endpoint, business rule, or repository query is added or changed, update the
  matching layer’s tests in the same change set.
- Shared starter handlers (validation, i18n) keep their own focused unit tests in
  `agentgo-springboot-starter` (or the owning module); do not duplicate those assertions inside
  every feature controller test.

#### Kotlin test method names

Define every unit test method name with Kotlin **backtick identifiers** so the name reads as a
short English sentence describing the behavior. Prefer this form over camelCase method names:

```kotlin
@Test
fun `create wraps service result with created response code and english message`() { … }

@Test
fun `create rejects duplicate workspace name for same owner`() { … }

@Test
fun `findByIdAndOwnerUserId returns only matching owner row`() { … }
```

Do not use vague names such as `test1`, `works`, or bare camelCase like `createsWorkspace`.
Keep names stable and specific to the assertion; one primary behavior per test method.

The build publishes exactly two executable Spring Boot Jars:

- `agentgo-app/build/libs/agentgo-app-*.jar`
- `agentgo-cli/build/libs/agentgo-cli-*.jar`

The commons, core, DTO, and starter modules are internal library modules and must not be
published as standalone release artifacts.

### Environments (dev / local / test)

Follow the workspace `AGENTS.md` promotion order: **dev → local → git/PR → test**.

| Env | How to run | Health / UI checks |
| --- | --- | --- |
| **dev** | Infra: `cd local-deployment && docker compose up -d` (no `--profile local`). App: copy `.bootRun.env.example` → `.bootRun.env`, then `./gradlew :agentgo-app:bootRun`. UI: Vite `:5173` with backend `http://localhost:8080`. | `curl http://localhost:8080/actuator/health` |
| **local** | `cd local-deployment && cp .env.example .env` then `docker compose --profile local up --build -d` (UI `:20080`, app `:20082`). | `curl http://localhost:20082/actuator/health` and open `http://localhost:20080` |
| **test** | GitHub Actions **Deployment TEST** (`workflow_dispatch` on `release/*`) SSHs to the TEST host and updates `$DEPLOY_PATH` Compose. Gateway `:28080` (`/ui`, `/backend`). | Verify login/logout on `http://{DEPLOY_HOST}:28080/ui` |

`local-deployment/docker-compose.yml` always can start shared infra (AgentGo Postgres `:5433`, Authentik `:9000`, MinIO). Services `agentgo-app`, `agentgo-ui`, and `authentik-oidc` require `--profile local`. Authentik uses its own PostgreSQL volume and must not reuse the AgentGo application database. Local Compose routes server-side OIDC token/JWKS through `authentik-oidc` so ID token `iss` is `http://localhost:9000/...` and RP-initiated logout `id_token_hint` validates.

Database schema changes must go through versioned Flyway migrations in `db-migration`; keep
`SPRING_JPA_HIBERNATE_DDL_AUTO=validate` in all environments. Keep Authentik credentials, client
secrets, SMTP settings, and generated data in the ignored `.env` / `.bootRun.env` and Docker volumes.
Do not copy credentials or data from other deployment repositories. Never commit `.env` or `.bootRun.env`.

Use `docker compose --profile local logs -f agentgo-app` when diagnosing local startup failures. Stop with
`docker compose --profile local down`; use `docker compose down -v` only when removing local database
data is intentional.

## Coding conventions

- Use Kotlin for new application and library code.
- Use constructor injection for Spring dependencies.
- Use programmatic transactions. Service implementations must open each transaction with
  `TransactionTemplate` or `PlatformTransactionManager` at the call site that owns the unit
  of work. Do not use declarative transactions: no `@Transactional` on classes or methods,
  including `readOnly`, `rollbackFor`, and other attributes. Set propagation and the
  read-only flag in that programmatic boundary so the transaction stays visible next to the
  work it covers. When editing a service that still uses `@Transactional`, convert that
  service in the same change.
- Define every thread pool explicitly. Do not submit work to `ForkJoinPool.commonPool()`,
  an unnamed `Executors` factory, `@Async` without an executor name, or a framework default
  task executor. Register each pool as a Spring bean in the owning module's `config`
  package. Give it a thread-name prefix that identifies the module and purpose, such as
  `agentgo-file-upload-`. Set the pool size, queue capacity, and rejection policy on that
  bean; do not leave them to framework defaults.
- Follow the official Kotlin coding conventions: use four-space indentation, `PascalCase`
  for classes and interfaces, `camelCase` for functions, properties, and parameters, and
  `UPPER_SNAKE_CASE` only for true compile-time constants. Prefer immutable `val` values,
  expression bodies where they improve readability, null-safe Kotlin types instead of
  platform-type workarounds, and standard-library collection operations over avoidable
  mutable state. Keep functions small and single-purpose, avoid unnecessary `!!`, and use
  sealed types or explicit result types when a finite state or failure model must be
  represented.
- Keep Kotlin imports explicit and organized by IDE/Kotlin formatter defaults. Do not use
  wildcard imports. Add KDoc to public classes, interfaces, functions, and properties when
  their behavior or contract is not self-evident; use `@param`, `@return`, and `@throws`
  where they add useful contract information.
- Keep exactly one top-level Kotlin declaration per source file. Each `class`, `data class`,
  `sealed class`, `enum class`, `interface`, `object`, and `typealias` must have its own file
  named after the declaration, including test declarations where practical.
- Keep Spring components focused and use constructor injection. Prefer data classes for
  immutable value objects and request/response models, and do not expose mutable collections
  from public APIs unless mutation is part of the contract.
- Every module below `agentgo-app-modules` must be named `agentgo-app-<module-name>` using
  lowercase kebab-case. The Gradle project path, module directory, and published artifact
  name must follow the same convention.
- Organize HTTP capabilities with an explicit Controller, Service, and Repository structure:
  - Each `XxxController` must have a matching `XxxControllerApiDoc` interface under the
    sibling `controller.apidoc` package. The API-doc
    type is an interface implemented by the controller and contains the Springdoc/Swagger
    contract, including tags, endpoint summaries/descriptions, parameters, request and
    response schemas, and relevant HTTP responses. Keep endpoint mappings and business logic
