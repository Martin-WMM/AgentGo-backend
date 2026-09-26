# AgentGo Backend

AgentGo Backend is a Kotlin, Gradle, Spring MVC, and Spring Boot 4 application.

The current project version is `0.1.0-SNAPSHOT`. Dependency and toolchain versions are centrally managed in the root [`build.gradle.kts`](build.gradle.kts).

## Modules

- `agentgo-app`: Core Spring Boot web service.
- `agentgo-commons`: Shared utility definitions and validation helpers.
- `agentgo-core`: Core AI workflow components and LangGraph4j integration.
- `agentgo-dto`: Cross-module data transfer objects and protocol models.
- `agentgo-springboot-starter`: Shared Spring Boot auto-configuration, beans, and logging foundations.
- `agentgo-cli`: Spring Shell command-line application for the `agentgo ...` command family.

The intended dependency direction is:

```text
agentgo-app  ─┬─> agentgo-core ─> agentgo-dto
              ├─> agentgo-springboot-starter ─> agentgo-commons, agentgo-dto
              └─> agentgo-dto

agentgo-cli  ─┬─> agentgo-core
              ├─> agentgo-springboot-starter
              └─> agentgo-dto
```

The baseline uses Spring Boot 4.1.1, Kotlin 2.3.x, Java 25 LTS, and Gradle 9.1+.

## Web service

The web service provides Actuator endpoints and OpenAPI documentation through Springdoc:

```text
GET /actuator/health
GET /actuator/info
GET /actuator/prometheus
GET /v3/api-docs
GET /swagger-ui.html
```

Health is provided by Actuator; the application does not define a custom health controller.

## CLI

Build and start the CLI Jar with:

```bash
java -jar agentgo-cli/build/libs/agentgo-cli-0.1.0-SNAPSHOT.jar
```

Example command:

```text
agentgo version
```

## Distributable artifacts

The build produces exactly two executable Spring Boot Jars:

- `agentgo-app/build/libs/agentgo-app-*.jar`: runnable web service.
- `agentgo-cli/build/libs/agentgo-cli-*.jar`: runnable `agentgo ...` shell CLI.

The commons, core, DTO, and starter modules are library modules and are not published as standalone Jars.

## Development workflow

Code changes follow: `main -> release/* -> feature/* or fix/* -> PR -> release/* -> PR -> main`.

- `main` and `release/*` accept changes only through pull requests.
- Every commit must use `<emoji><type>: <message>` and change fewer than 300 lines.
- Every pull request must pass Merge CI, tests, and the 85% code coverage gate.

## Local build

Install JDK 25 and Gradle 9.1 or newer, then run:

```bash
gradle build
```

Windows PowerShell:

```powershell
gradle build
```

The build creates the two executable application Jars described above. CI runs the same build with the pinned Gradle version from the workflow.

## Environment variables

| Variable | Default | Purpose |
| --- | --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/agentgo` | PostgreSQL JDBC URL |
| `DATABASE_USERNAME` | `agentgo` | PostgreSQL username |
| `DATABASE_PASSWORD` | `agentgo` | PostgreSQL password |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4318/v1/traces` | OTLP trace endpoint |
| `TRACING_SAMPLING_PROBABILITY` | `0.1` | Trace sampling probability |
| `OPENAI_API_KEY` | unset | Spring AI OpenAI model access |
