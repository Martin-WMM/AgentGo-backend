# Local deployment

Compose stack under `AgentGo-backend/local-deployment` supports two host modes that share
Authentik / Postgres / MinIO, plus the remote **test** environment documented in the workspace
`AGENTS.md`.

| Mode | Command | UI | Backend |
| --- | --- | --- | --- |
| **dev** (infra only) | `docker compose up -d` | host Vite `:5173` | host `bootRun` `:8080` |
| **local** (full Docker) | `docker compose --profile local up --build -d` | `:20080` | `:20082` |

The pinned Authentik version is `2026.8.3`. Keep the server and worker on the same version.

## Shared infra (dev)

```powershell
Copy-Item .env.example .env   # once
docker compose up -d
Copy-Item .bootRun.env.example .bootRun.env
# fill AGENTGO_OIDC_CLIENT_SECRET in .bootRun.env
cd ..
.\gradlew.bat :agentgo-app:bootRun
# other terminal: pnpm --dir ..\AgentGo-UI dev
```

Do **not** start `--profile local` while using host `:8080`, and do **not** export `.bootRun.env`
into the shell before a Compose `local` up (Compose inherits the process environment).

## Full local stack

```powershell
Copy-Item .env.example .env
$env:VITE_AGENTGO_BACKEND_URL = "http://localhost:20082"
pnpm --dir ..\..\AgentGo-UI build
.\scripts\sync-ui-dist.ps1
..\gradlew.bat :agentgo-app:bootJar
Copy-Item ..\agentgo-app\build\libs\agentgo-app-*.jar .\agentgo-app.jar -Force
docker compose --profile local up --build -d
```

| Service | URL |
| --- | --- |
| UI | <http://localhost:20080> |
| Backend | <http://localhost:20082> |
| Authentik | <http://localhost:9000> |

`agentgo-app` calls Authentik token/JWKS/userinfo through the internal `authentik-oidc` nginx
proxy (Host rewritten to `localhost:9000`) so ID token `iss` matches browser end-session and
logout with `id_token_hint` succeeds.

Open Authentik initial setup at <http://localhost:9000/if/flow/initial-setup/> (trailing slash
required). Never commit `.env` or `.bootRun.env`.

## Verify and stop

```bash
docker compose --profile local ps
curl http://localhost:20082/actuator/health
curl http://localhost:20080/
docker compose --profile local logs -f agentgo-app
docker compose --profile local down
```

Use `docker compose down -v` only when removing local database volumes is intentional.
