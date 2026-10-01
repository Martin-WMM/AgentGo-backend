output "application_slug" {
  description = "The Authentik application slug."
  value       = authentik_application.agentgo.slug
}

output "issuer_url" {
  description = "OIDC issuer URL for the AgentGo provider."
  value       = "${trimsuffix(var.authentik_url, "/")}/application/o/${authentik_application.agentgo.slug}/"
}

output "client_id" {
  description = "OIDC client ID for AgentGo."
  value       = authentik_provider_oauth2.agentgo.client_id
}

output "client_secret" {
  description = "OIDC client secret for the confidential AgentGo backend client."
  value       = authentik_provider_oauth2.agentgo.client_secret
  sensitive   = true
}
