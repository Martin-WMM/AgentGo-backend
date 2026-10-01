# Read the existing brand first so it can be imported into Terraform state.
data "authentik_brand" "existing_agentgo_default" {
  domain = "authentik-default"
}

# Adopt the existing local AgentGo brand and make the AgentGo login flow the
# instance-wide default used after logout and for direct Authentik entry.
resource "authentik_brand" "agentgo_default" {
  domain              = data.authentik_brand.existing_agentgo_default.domain
  default             = data.authentik_brand.existing_agentgo_default.default
  branding_title      = data.authentik_brand.existing_agentgo_default.branding_title
  branding_logo       = data.authentik_brand.existing_agentgo_default.branding_logo
  branding_favicon    = data.authentik_brand.existing_agentgo_default.branding_favicon
  flow_authentication = authentik_flow.agentgo_authentication.uuid
  flow_invalidation   = data.authentik_brand.existing_agentgo_default.flow_invalidation
  flow_user_settings  = data.authentik_brand.existing_agentgo_default.flow_user_settings
}
