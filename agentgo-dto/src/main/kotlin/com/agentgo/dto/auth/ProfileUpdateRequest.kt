package com.agentgo.dto.auth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Editable fields for the authenticated user's profile")
data class ProfileUpdateRequest(
    @field:Schema(description = "User's display name", example = "Ada Lovelace", minLength = 1, maxLength = 150)
    val displayName: String,
    @field:Schema(description = "User's email address", example = "ada@example.com", format = "email", maxLength = 254)
    val email: String?,
    @field:Schema(description = "Optional avatar URL", example = "https://example.com/avatar.png", format = "uri", maxLength = 2048)
    val avatarUrl: String?,
)
