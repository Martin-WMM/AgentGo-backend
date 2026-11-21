package com.agentgo.dto.auth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "The authenticated user's profile. Identity fields are synchronized from Authentik. Extended fields are stored by AgentGo for later agent access.")
data class ProfileResponse(
    @field:Schema(description = "Stable username managed by Authentik", example = "ada.lovelace")
    val username: String,
    @field:Schema(description = "User's display name", example = "Ada Lovelace")
    val displayName: String,
    @field:Schema(description = "User's email address", example = "ada@example.com", format = "email")
    val email: String?,
    @field:Schema(description = "Optional avatar URL", example = "https://example.com/avatar.png", format = "uri")
    val avatarUrl: String?,
    @field:Schema(description = "Optional phone number stored for agent context", example = "+1 202 555 0100")
    val phoneNumber: String? = null,
    @field:Schema(description = "Optional personal description stored for agent context", example = "Mathematician working on analytical engines.")
    val description: String? = null,
    @field:Schema(description = "Optional occupation or job title", example = "Mathematician")
    val occupation: String? = null,
    @field:Schema(description = "Optional organization or employer", example = "Analytical Engines")
    val organization: String? = null,
    @field:Schema(description = "Optional personal or organization homepage", example = "https://example.com/ada", format = "uri")
    val homepageUrl: String? = null,
    @field:Schema(description = "Optional free-form location", example = "London")
    val location: String? = null,
    @field:Schema(description = "Optional IANA time zone id", example = "Europe/London")
    val timezone: String? = null,
    @field:Schema(description = "Optional preferred BCP 47 language tag", example = "en")
    val preferredLanguage: String? = null,
)
