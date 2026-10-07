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

variable "ui_base_url" {
  description = "Public base URL of the AgentGo UI used for Authentik launch metadata."
  type        = string
  default     = "http://localhost:5173"
}

variable "minio_server" {
  description = "MinIO S3 API host:port, for example 127.0.0.1:20910."
  type        = string
  default     = "127.0.0.1:9000"
}

variable "minio_user" {
  description = "MinIO root or admin access key."
  type        = string
  sensitive   = true
  default     = null
}

variable "minio_password" {
  description = "MinIO root or admin secret key."
  type        = string
  sensitive   = true
  default     = null
}

variable "minio_ssl" {
  description = "Whether the MinIO endpoint uses TLS."
  type        = bool
  default     = false
}

variable "minio_bucket" {
  description = "Application bucket created for AgentGo file storage."
  type        = string
  default     = "agentgo-bucket"
}
