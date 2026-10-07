# AgentGo Authentik and MinIO Terraform

This directory manages the Authentik objects and MinIO bucket used by AgentGo.
It is intentionally separate from `local-deployment/`: Docker Compose runs
Authentik, PostgreSQL, and MinIO, while Terraform configures Authentik through
its API and creates the application bucket.

## What is managed

- An AgentGo authentication flow.
- Identification, password, and user-login stages and their bindings.
- Links from the login form to Authentik's default enrollment and recovery flows.
- An OAuth2/OIDC provider and an AgentGo application.
- The MinIO `agentgo-bucket` used by AgentGo file storage.

The configuration creates its own authorization, invalidation, enrollment, and
recovery flows. It does not depend on optional default-flow blueprints being
present after an Authentik installation.

## Prerequisites

1. Start `local-deployment/` (or the TEST Compose stack) and complete Authentik's initial setup.
2. Create an Authentik API token with permission to manage flows and applications.
3. Install Terraform 1.6 or newer.
4. Provide MinIO admin credentials (`minio_user` / `minio_password`) for bucket creation.

For the shared TEST host, use GitHub Actions **Terraform TEST** instead of applying
from a laptop. See `environments/test/README.md`.

The Authentik Terraform provider version is pinned to the `2026.8` minor line.
Provider versions track Authentik releases; update the constraint deliberately
when upgrading the Authentik image.

## Usage

From this directory:

```powershell
$env:AUTHENTIK_URL = "http://localhost:9000"
$env:AUTHENTIK_TOKEN = "<api-token>"
terraform init
terraform plan
terraform apply
```

Alternatively, copy `terraform.tfvars.example` to `terraform.auto.tfvars` and
fill in the token locally. All `*.tfvars` files except the example are ignored.

Set `client_id`, `client_secret`, and `redirect_uris` only when the deployment
needs stable client credentials or non-local callback URLs. The generated client
secret is sensitive and is not exposed as an output.

The default recovery flow sends a password-reset email after the user identifies
their account. Configure Authentik SMTP settings before testing email delivery;
the local Compose stack exposes those settings through `local-deployment/.env`.

## State and secrets

Do not commit `.terraform/`, `*.tfstate`, `terraform.tfvars`, API tokens, client
secrets, or production redirect URIs. Terraform state can contain sensitive
provider data; use an encrypted remote backend for shared environments. The
repository deliberately does not configure a backend because the backend choice
belongs to each deployment environment.

## Feasibility boundary

Terraform can declaratively manage Authentik API objects after Authentik is
available. It does not provision Authentik's PostgreSQL database, initial admin
account, or Docker volumes. Keep those responsibilities in `local-deployment/`
or in the target platform's infrastructure layer.
