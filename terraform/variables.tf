variable "authentik_url" {
  description = "Base URL of the Authentik instance, for example http://localhost:9000."
  type        = string
  default     = "http://localhost:9000"
}

variable "authentik_token" {
  description = "Authentik API token. Keep this in an ignored local tfvars file or inject it through the environment."
  type        = string
  sensitive   = true
  default     = null
}

variable "application_name" {
  description = "Display name of the AgentGo Authentik application."
  type        = string
  default     = "AgentGo"
}

variable "application_slug" {
  description = "Stable Authentik slug for the AgentGo application."
  type        = string
  default     = "agentgo"
}

variable "redirect_uris" {
  description = "OIDC redirect URIs for the AgentGo UI."
  type = list(object({
    url               = string
    matching_mode     = optional(string, "strict")
    redirect_uri_type = optional(string, "authorization")
  }))
  default = [
    {
      url               = "http://localhost:8080/login/oauth2/code/authentik"
      matching_mode     = "strict"
      redirect_uri_type = "authorization"
    },
    {
      url               = "http://localhost:5173"
      matching_mode     = "strict"
      redirect_uri_type = "logout"
    },
    {
      url               = "http://localhost:5173/"
      matching_mode     = "strict"
      redirect_uri_type = "logout"
    },
    {
      url               = "http://localhost:5173/?signedOut=1"
      matching_mode     = "strict"
      redirect_uri_type = "logout"
    }
  ]
}

variable "client_id" {
  description = "Stable OIDC client ID for AgentGo."
  type        = string
  default     = "agentgo"
}

variable "client_secret" {
  description = "OIDC client secret. Leave null to let Authentik generate one."
  type        = string
  sensitive   = true
  default     = null
}
