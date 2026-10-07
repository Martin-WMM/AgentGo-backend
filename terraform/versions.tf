terraform {
  required_version = ">= 1.6.0"

  required_providers {
    authentik = {
      source  = "goauthentik/authentik"
      version = "~> 2026.8.0"
    }
    minio = {
      source  = "aminueza/minio"
      version = "~> 3.44.0"
    }
  }
}
