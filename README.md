# AgentGo Backend

AgentGo Backend is a Kotlin, Gradle, Spring MVC, and Spring Boot 4 application.

## Development workflow

Code changes follow: `main → release/* → feature/* or fix/* → PR → release/* → PR → main`.

- `main` and `release/*` accept changes only through pull requests.
- Every commit must use `<emoji><type>: <message>` and change fewer than 300 lines.
- Every pull request must pass Merge CI, tests, and the 85% code coverage gate.

## Local build

Install JDK 21 and run:

```bash
./gradlew build
```

Windows PowerShell:

```powershell
.\gradlew.bat build
```

## Application

- `agentgo-app` is the only project module.
- It provides the Spring MVC HTTP API.
- The baseline uses Spring Boot 4.1.1, Kotlin 2.2.x, Java 21, and Gradle 8.14+.
- Actuator exposes health, info, and Prometheus metrics endpoints.
- Micrometer Tracing exports OTLP traces when an OpenTelemetry collector is available.
- Spring Data JPA and PostgreSQL are configured through environment variables.
- Spring AI Core and LangGraph4j are available for agent workflow integration.

## Health endpoint

```text
GET /api/v1/health
```

## Infrastructure endpoints

```text
GET /actuator/health
GET /actuator/info
GET /actuator/prometheus
```

## Environment variables

| Variable | Default | Purpose |
| --- | --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/agentgo` | PostgreSQL JDBC URL |
| `DATABASE_USERNAME` | `agentgo` | PostgreSQL username |
| `DATABASE_PASSWORD` | `agentgo` | PostgreSQL password |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4318/v1/traces` | OTLP trace endpoint |
| `TRACING_SAMPLING_PROBABILITY` | `0.1` | Trace sampling probability |
| `OPENAI_API_KEY` | unset | Spring AI OpenAI model access |
