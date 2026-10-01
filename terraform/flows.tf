# AgentGo-owned flows make a fresh Authentik install reproducible.

resource "authentik_flow" "agentgo_authentication" {
  name           = "AgentGo Authentication"
  title          = "Sign in to AgentGo"
  slug           = "agentgo-authentication"
  designation    = "authentication"
  authentication = "require_unauthenticated"
}

resource "authentik_flow" "agentgo_enrollment" {
  name           = "AgentGo Enrollment"
  title          = "Create your AgentGo account"
  slug           = "agentgo-enrollment"
  designation    = "enrollment"
  authentication = "require_unauthenticated"
}

resource "authentik_flow" "agentgo_recovery" {
  name           = "AgentGo Password Recovery"
  title          = "Reset your AgentGo password"
  slug           = "agentgo-recovery"
  designation    = "recovery"
  authentication = "require_unauthenticated"
}

resource "authentik_flow" "agentgo_authorization" {
  name        = "AgentGo Authorization"
  title       = "Authorize AgentGo"
  slug        = "agentgo-authorization"
  designation = "authorization"
}

resource "authentik_flow" "agentgo_invalidation" {
  name        = "AgentGo Invalidation"
  title       = "Sign out of AgentGo"
  slug        = "agentgo-invalidation"
  designation = "invalidation"
}

# RP-initiated logout only plans this flow. Without a user-logout stage the
# Authentik browser session stays alive and the UI signs the user back in.
resource "authentik_stage_user_logout" "agentgo_user_logout" {
  name = "AgentGo User Logout"
}

resource "authentik_flow_stage_binding" "agentgo_invalidation_logout" {
  target = authentik_flow.agentgo_invalidation.uuid
  stage  = authentik_stage_user_logout.agentgo_user_logout.id
  order  = 0
}

# After the session ends, return to the UI. The UI starts a new OIDC login, so a
# successful sign-in comes back to the UI instead of Authentik's own home page.
resource "authentik_stage_redirect" "agentgo_logout_redirect" {
  name          = "AgentGo Logout Redirect"
  mode          = "static"
  target_static = "http://localhost:5173"
  keep_context  = false
}

resource "authentik_flow_stage_binding" "agentgo_invalidation_redirect" {
  target = authentik_flow.agentgo_invalidation.uuid
  stage  = authentik_stage_redirect.agentgo_logout_redirect.id
  order  = 10
}

resource "authentik_stage_password" "agentgo_password" {
  name     = "AgentGo Password"
  backends = ["authentik.core.auth.InbuiltBackend"]
}

resource "authentik_stage_identification" "agentgo_identification" {
  name                      = "AgentGo Identification"
  user_fields               = ["username", "email"]
  password_stage            = authentik_stage_password.agentgo_password.id
  enrollment_flow           = authentik_flow.agentgo_enrollment.uuid
  recovery_flow             = authentik_flow.agentgo_recovery.uuid
  case_insensitive_matching = true
  show_matched_user         = true
}

resource "authentik_stage_user_login" "agentgo_user_login" {
  name = "AgentGo User Login"
}

resource "authentik_flow_stage_binding" "agentgo_identification" {
  target = authentik_flow.agentgo_authentication.uuid
  stage  = authentik_stage_identification.agentgo_identification.id
  order  = 10
}

resource "authentik_flow_stage_binding" "agentgo_user_login" {
  target = authentik_flow.agentgo_authentication.uuid
  stage  = authentik_stage_user_login.agentgo_user_login.id
  order  = 20
}

# Enrollment: collect username, email, and password, then verify the email.
resource "authentik_stage_prompt_field" "enrollment_username" {
  name      = "AgentGo Enrollment Username"
  field_key = "username"
  label     = "Username"
  type      = "username"
  required  = true
  order     = 0
}

resource "authentik_stage_prompt_field" "enrollment_password" {
  name      = "AgentGo Enrollment Password"
  field_key = "password"
  label     = "Password"
  type      = "password"
  required  = true
  order     = 0
}

resource "authentik_stage_prompt_field" "enrollment_password_repeat" {
  name      = "AgentGo Enrollment Password Repeat"
  field_key = "password_repeat"
  label     = "Password (repeat)"
  type      = "password"
  required  = true
  order     = 1
}

resource "authentik_stage_prompt_field" "enrollment_email" {
  name      = "AgentGo Enrollment Email"
  field_key = "email"
  label     = "Email"
  type      = "email"
  required  = true
  order     = 0
}

resource "authentik_stage_prompt" "enrollment_account" {
  name = "AgentGo Enrollment Account"
  fields = [
    authentik_stage_prompt_field.enrollment_username.id,
    authentik_stage_prompt_field.enrollment_password.id,
    authentik_stage_prompt_field.enrollment_password_repeat.id,
    authentik_stage_prompt_field.enrollment_email.id,
  ]
}

resource "authentik_stage_user_write" "enrollment_user_write" {
  name                     = "AgentGo Enrollment User Write"
  create_users_as_inactive = true
  user_creation_mode       = "always_create"
}

resource "authentik_stage_email" "enrollment_verification" {
  name                     = "AgentGo Enrollment Email Verification"
  use_global_settings      = true
  template                 = "email/account_confirmation.html"
  activate_user_on_success = true
}

resource "authentik_flow_stage_binding" "enrollment_prompt" {
  target = authentik_flow.agentgo_enrollment.uuid
  stage  = authentik_stage_prompt.enrollment_account.id
  order  = 10
}

resource "authentik_flow_stage_binding" "enrollment_user_write" {
  target = authentik_flow.agentgo_enrollment.uuid
  stage  = authentik_stage_user_write.enrollment_user_write.id
  order  = 20
}

resource "authentik_flow_stage_binding" "enrollment_email" {
  target = authentik_flow.agentgo_enrollment.uuid
  stage  = authentik_stage_email.enrollment_verification.id
  order  = 30
}

resource "authentik_flow_stage_binding" "enrollment_login" {
  target = authentik_flow.agentgo_enrollment.uuid
  stage  = authentik_stage_user_login.agentgo_user_login.id
  order  = 100
}

# Recovery: identify the user, send a reset email, then write the new password.
resource "authentik_stage_identification" "recovery_identification" {
  name        = "AgentGo Recovery Identification"
  user_fields = ["email", "username"]
}

resource "authentik_stage_email" "recovery_email" {
  name                     = "AgentGo Recovery Email"
  use_global_settings      = true
  template                 = "email/password_reset.html"
  token_expiry             = "minutes=30"
  recovery_max_attempts    = 5
  recovery_cache_timeout   = "minutes=5"
  subject                  = "AgentGo password reset"
  activate_user_on_success = true
}

resource "authentik_stage_prompt_field" "recovery_password" {
  name      = "AgentGo Recovery Password"
  field_key = "password"
  label     = "New password"
  type      = "password"
  required  = true
  order     = 0
}

resource "authentik_stage_prompt_field" "recovery_password_repeat" {
  name      = "AgentGo Recovery Password Repeat"
  field_key = "password_repeat"
  label     = "New password (repeat)"
  type      = "password"
  required  = true
  order     = 1
}

resource "authentik_stage_prompt" "recovery_password_prompt" {
  name = "AgentGo Recovery Password Prompt"
  fields = [
    authentik_stage_prompt_field.recovery_password.id,
    authentik_stage_prompt_field.recovery_password_repeat.id,
  ]
}

resource "authentik_stage_user_write" "recovery_user_write" {
  name               = "AgentGo Recovery User Write"
  user_creation_mode = "never_create"
}

resource "authentik_flow_stage_binding" "recovery_identification" {
  target               = authentik_flow.agentgo_recovery.uuid
  stage                = authentik_stage_identification.recovery_identification.id
  order                = 10
  re_evaluate_policies = true
}

resource "authentik_flow_stage_binding" "recovery_email" {
  target               = authentik_flow.agentgo_recovery.uuid
  stage                = authentik_stage_email.recovery_email.id
  order                = 20
  re_evaluate_policies = true
}

resource "authentik_flow_stage_binding" "recovery_password_prompt" {
  target = authentik_flow.agentgo_recovery.uuid
  stage  = authentik_stage_prompt.recovery_password_prompt.id
  order  = 30
}

resource "authentik_flow_stage_binding" "recovery_user_write" {
  target = authentik_flow.agentgo_recovery.uuid
  stage  = authentik_stage_user_write.recovery_user_write.id
  order  = 40
}

resource "authentik_flow_stage_binding" "recovery_login" {
  target = authentik_flow.agentgo_recovery.uuid
  stage  = authentik_stage_user_login.agentgo_user_login.id
  order  = 100
}
