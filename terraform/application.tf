data "authentik_property_mapping_provider_scope" "agentgo_oidc_scopes" {
  managed_list = [
    "goauthentik.io/providers/oauth2/scope-openid",
    "goauthentik.io/providers/oauth2/scope-profile",
    "goauthentik.io/providers/oauth2/scope-email",
  ]
}

data "authentik_certificate_key_pair" "agentgo_signing_key" {
  name              = "authentik Self-signed Certificate"
  fetch_key         = false
  fetch_certificate = false
}

resource "authentik_provider_oauth2" "agentgo" {
  name                = var.application_name
  client_id           = var.client_id
  client_secret       = var.client_secret
  authentication_flow = authentik_flow.agentgo_authentication.uuid
  authorization_flow  = authentik_flow.agentgo_authorization.uuid
  invalidation_flow   = authentik_flow.agentgo_invalidation.uuid
  property_mappings   = data.authentik_property_mapping_provider_scope.agentgo_oidc_scopes.ids
  signing_key         = data.authentik_certificate_key_pair.agentgo_signing_key.id

  allowed_redirect_uris      = var.redirect_uris
  client_type                = "confidential"
  include_claims_in_id_token = true
  issuer_mode                = "per_provider"
  logout_method              = "backchannel"
  sub_mode                   = "hashed_user_id"
}

resource "authentik_application" "agentgo" {
  name              = var.application_name
  slug              = var.application_slug
  protocol_provider = authentik_provider_oauth2.agentgo.id
  meta_launch_url   = var.ui_base_url
  meta_icon         = "${trimsuffix(var.ui_base_url, "/")}/assets/logo-dark.png"
  meta_description  = "AgentGo web application"
}
