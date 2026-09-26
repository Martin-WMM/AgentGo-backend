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

The local stack overrides `SPRING_JPA_HIBERNATE_DDL_AUTO` with `update` because the
application does not yet include database migration scripts. The default application
configuration remains `validate`.
