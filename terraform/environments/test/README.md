# Terraform TEST environment

This directory documents the TEST apply target. Sources live in `terraform/` and
are applied on the TEST host by the **Terraform TEST** workflow.

## Target

- Host: GitHub `test` environment `DEPLOY_HOST`
- Authentik API: `http://127.0.0.1:20900` on the TEST host
- MinIO S3 API: `http://127.0.0.1:20910` on the TEST host
- Public gateway: `http://DEPLOY_HOST:28080`

## Required GitHub `test` secrets

| Secret | Purpose |
| --- | --- |
| `DEPLOY_HOST` / `DEPLOY_PORT` / `DEPLOY_USER` / `DEPLOY_PATH` | SSH target used by Deployment TEST |
| `DEPLOY_SSH_KEY` / `DEPLOY_KNOWN_HOSTS` | SSH credentials |
| `AUTHENTIK_TOKEN` | Authentik API token with permission to manage flows and applications |

MinIO credentials are read from `$DEPLOY_PATH/.env` on the TEST host
(`MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD`) so they stay aligned with Compose.

## Apply

From a `release/*` branch, run **Actions → Terraform TEST → Run workflow**.
