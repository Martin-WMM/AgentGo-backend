package com.agentgo.dto.auth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "The current AgentGo authentication session")
data class SessionResponse(
    @field:Schema(description = "Whether the current request is authenticated", example = "true")
    val authenticated: Boolean,
    @field:Schema(description = "Stable subject identifier from the OIDC provider", example = "user-123")
    val subject: String? = null,
    @field:Schema(description = "Display name of the authenticated user", example = "Ada Lovelace")
    val name: String? = null,
    @field:Schema(description = "Email address of the authenticated user", example = "ada@example.com", format = "email")
    val email: String? = null,
    @field:Schema(description = "Optional profile image URL", example = "https://example.com/avatar.png", format = "uri")
    val picture: String? = null,
)
