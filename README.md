# AgentGo Backend

AgentGo Backend is a Kotlin, Gradle, Spring MVC, and Spring Boot 4 application.

The current project version is `0.1.0-SNAPSHOT`. Dependency and toolchain versions are centrally managed in the root [`build.gradle.kts`](build.gradle.kts).

## Modules

- `agentgo-app`: Core Spring Boot web service.
- `agentgo-app-modules`: Composable application capability starters used by `agentgo-app`.
- `agentgo-commons`: Shared utility definitions and validation helpers.
- `agentgo-core`: Core AI workflow components and LangGraph4j integration.
- `agentgo-dto`: Cross-module data transfer objects and protocol models.
- `agentgo-springboot-starter`: Shared Spring Boot auto-configuration, beans, and logging foundations.
- `agentgo-cli`: Spring Shell command-line application for the `agentgo ...` command family.

The `agentgo-app-modules` group currently contains:

- `agentgo-web-starter`: Spring MVC and Springdoc OpenAPI.
- `agentgo-observability-starter`: Actuator, Prometheus, and OpenTelemetry tracing.
- `agentgo-persistence-starter`: Spring Data JPA and PostgreSQL runtime support.
- `agentgo-ai-starter`: Spring AI and AgentGo core workflow integration.

The intended dependency direction is:

```text
agentgo-app  ─> agentgo-app-modules/*

agentgo-app-modules/* ─> agentgo-springboot-starter
agentgo-ai-starter     ─> agentgo-core ─> agentgo-dto
agentgo-springboot-starter ─> agentgo-commons, agentgo-dto

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

## Local deployment

The [`local-deployments`](local-deployments) directory contains a Docker Compose stack for
the web service and PostgreSQL:

```bash
cd local-deployments
cp .env.example .env
docker compose up --build -d
```

Verify the running service with:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/v3/api-docs
```

See [`local-deployments/README.md`](local-deployments/README.md) for logs, shutdown, and data-volume commands.

The [`local-deployment`](local-deployment) directory contains the separate local Authentik
identity-center stack. It uses its own PostgreSQL volume and exposes the Authentik setup flow
at `http://localhost:9000/if/flow/initial-setup/`. The [`terraform`](terraform) directory
provisions the AgentGo application, OIDC provider, login flow, email registration flow, and
password-recovery flow. Apply it after Authentik is available, then copy the generated client
secret into `local-deployments/.env`.

The backend exposes `/api/auth/session`, `/api/auth/login`, `/api/auth/logout`, and the authenticated
profile endpoints `/api/auth/profile` (`GET` and `PUT`). Profile updates are written to Authentik
through its Core User API. Configure `AGENTGO_AUTHENTIK_API_TOKEN` with a server-side Authentik API
token that can manage users; never expose this token to the UI or commit it. Logout clears the
AgentGo session and redirects the browser to Authentik's RP-initiated logout endpoint. That ends
the Authentik session and returns to the UI, which starts a new sign-in. After the user signs in,
Authentik sends them back to the UI.

## Environment variables

| Variable | Default | Purpose |
| --- | --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/agentgo` | PostgreSQL JDBC URL |
| `DATABASE_USERNAME` | `agentgo` | PostgreSQL username |
| `DATABASE_PASSWORD` | `agentgo` | PostgreSQL password |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4318/v1/traces` | OTLP trace endpoint |
| `TRACING_SAMPLING_PROBABILITY` | `0.1` | Trace sampling probability |
| `OPENAI_API_KEY` | unset | Spring AI OpenAI model access |
| `AGENTGO_OIDC_ISSUER_URI` | `http://localhost:9000/application/o/agentgo/` | Authentik OIDC issuer; use `host.docker.internal` when the backend runs in Docker |
| `AGENTGO_OIDC_CLIENT_ID` | `agentgo` | Authentik confidential client ID |
| `AGENTGO_OIDC_CLIENT_SECRET` | unset | Secret generated by the Terraform Authentik provider |
| `AGENTGO_UI_BASE_URL` | `http://localhost:5173` | UI URL used after login and logout |
| `AGENTGO_AUTHENTIK_API_BASE_URL` | `http://localhost:9000` | Authentik Core API base URL |
| `AGENTGO_AUTHENTIK_API_TOKEN` | unset | Server-side token used to synchronize user profiles |
| `AGENTGO_AUTHENTIK_BROWSER_BASE_URL` | `http://localhost:9000` | Authentik base URL reachable by the browser for OIDC logout redirects |
| `AGENTGO_OPENAPI_TITLE` | `AgentGo API` | OpenAPI document title |
| `AGENTGO_OPENAPI_DESCRIPTION` | `AgentGo backend service API` | OpenAPI document description |
| `AGENTGO_OPENAPI_VERSION` | `v1` | API document version |
| `AGENTGO_OPENAPI_SERVER_URL` | unset | Optional server URL shown by Swagger UI |
