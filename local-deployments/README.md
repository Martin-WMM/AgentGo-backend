# Local deployment

This Compose stack starts a local PostgreSQL database and the executable `agentgo-app`
Spring Boot service.

## Start

From this directory:

```bash
cp .env.example .env
docker compose up --build -d
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
docker compose up --build -d
```

## Verify

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/v3/api-docs
```

The Swagger UI is available at <http://localhost:8080/swagger-ui.html>.

Inspect service status and logs with:

```bash
docker compose ps
docker compose logs -f agentgo-app
```

## Stop

```bash
docker compose down
```

To remove the local PostgreSQL data volume as well:

```bash
docker compose down -v
```

Database changes are applied by Flyway from the repository's `db-migration` directory.
The local stack uses `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`, matching the default
application configuration.

The default `OPENAI_API_KEY` is a local placeholder that only allows Spring AI to initialize.
Set a real key in `.env` before testing model calls.
