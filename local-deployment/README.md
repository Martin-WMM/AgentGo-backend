# Local deployment

This Compose stack is the single local runtime for AgentGo development. It runs:

- AgentGo application PostgreSQL (`agentgo-postgresql`, host port `5433` by default)
- Optional `agentgo-app` container image
- Authentik server and worker with their own PostgreSQL volume
- MinIO object storage for local file assets

The pinned Authentik version is `2026.8.3`. Keep the server and worker on the same version.

The default Authentik brand is configured as `AgentGo` and uses the Logo stored in
`branding/agentgo-logo.png`. The file is mounted read-only into Authentik's public media directory so
container recreation does not remove the brand asset.

The default brand is also declared in `blueprints/agentgo-brand.yaml`, so a fresh local database
receives the same title and logo during Authentik worker startup.

## Start

From `AgentGo-backend/local-deployment`:

```bash
cp .env.example .env
docker compose pull
docker compose up -d
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
docker compose pull
docker compose up -d
```

To also build and run the backend container:

```bash
docker compose up --build -d
```

Open the Authentik initial setup flow at:

<http://localhost:9000/if/flow/initial-setup/>

The trailing slash is required by Authentik's initial setup flow. Create the local
`akadmin` account in the browser; credentials are not stored in this repository.

When running the backend on the host (IDE / `bootJar`) instead of the Compose app service,
point Spring at the published AgentGo database port:

```text
DATABASE_URL=jdbc:postgresql://localhost:5433/agentgo
DATABASE_USERNAME=agentgo
DATABASE_PASSWORD=agentgo-local
```

## MinIO object storage

MinIO is available for local file and object-storage development:

- S3 API: <http://localhost:9001>
- MinIO Console: <http://localhost:9002>
- Persistent data volume: `minio-data`

Use `MINIO_ROOT_USER` and `MINIO_ROOT_PASSWORD` from the local `.env` file to sign in. The
default values in `.env.example` are for local development only and must be changed before
sharing the deployment or using it outside a local machine.

## Configure AgentGo as an OIDC client

After the initial setup, create an OAuth2/OpenID Provider and an Application in Authentik,
or apply the repository Terraform under `terraform/`. Store the generated client secret in
the ignored `local-deployment/.env` as `AGENTGO_OIDC_CLIENT_SECRET`.

## Verify and stop

```bash
docker compose ps
curl http://localhost:8080/actuator/health
curl http://localhost:8080/swagger-ui.html
docker compose logs -f agentgo-app
docker compose down
```

Use `docker compose down -v` only when removing local database volumes is intentional.
Never commit `.env`.
