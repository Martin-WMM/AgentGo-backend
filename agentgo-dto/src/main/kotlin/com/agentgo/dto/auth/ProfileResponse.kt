package com.agentgo.dto.auth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "The authenticated user's editable profile")
data class ProfileResponse(
    @field:Schema(description = "Stable username managed by Authentik", example = "ada.lovelace")
    val username: String,
    @field:Schema(description = "User's display name", example = "Ada Lovelace")
    val displayName: String,
    @field:Schema(description = "User's email address", example = "ada@example.com", format = "email")
    val email: String?,
    @field:Schema(description = "Optional avatar URL", example = "https://example.com/avatar.png", format = "uri")
    val avatarUrl: String?,
)
