provider "authentik" {
  # Values can come from ignored local tfvars or the provider's
  # AUTHENTIK_URL/AUTHENTIK_TOKEN environment variables.
  url   = var.authentik_url
  token = var.authentik_token
}
