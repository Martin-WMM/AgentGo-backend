# Local Authentik deployment

This Compose stack provides the local Authentik identity center for AgentGo. It runs:

- Authentik server and worker
- PostgreSQL for Authentik state

The deployment is intentionally isolated from the application database stack in
`local-deployments/`.

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

Open the initial setup flow at:

<http://localhost:9000/if/flow/initial-setup/>

The trailing slash is required by Authentik's initial setup flow. Create the local
`akadmin` account in the browser; credentials are not stored in this repository.

## Configure AgentGo as an OIDC client

After the initial setup, create an OAuth2/OpenID Provider and an Application in Authentik:

1. Use `AgentGo` as the application name.
2. Choose the OAuth2/OpenID Provider type.
3. Add the AgentGo UI callback URL, for example
   `http://localhost:5173/auth/callback`.
4. Store the generated client ID and client secret outside Git.

The provider issuer follows this form:

```text
http://localhost:9000/application/o/<provider-slug>/
```

The backend will use this issuer when its Spring Security OAuth2 resource-server integration
is enabled. The current backend initialization contains the identity-center deployment; API
authentication wiring will be added alongside the backend security module and its contract.

## Verify and stop

```bash
docker compose ps
docker compose logs -f authentik-server
docker compose down
```

To remove local Authentik data as well, use `docker compose down -v`. This permanently removes
the local identity database and should only be used for a deliberate reset.

## Security notes

- Never commit `.env`, Authentik data, client secrets, SMTP credentials, or production keys.
- Generate a unique `AUTHENTIK_SECRET_KEY` and PostgreSQL password outside local-only testing.
- The Docker socket is not mounted because this deployment does not manage Authentik outposts.
- The original ContextX deployment contains credentials and data directories; they were not
  copied into AgentGo.
