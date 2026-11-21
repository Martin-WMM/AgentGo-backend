package com.agentgo.auth.controller.apidoc

import com.agentgo.dto.auth.SessionResponse
import com.agentgo.dto.auth.ProfileResponse
import com.agentgo.dto.auth.ProfileUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Tag(
    name = "Authentication",
    description = "Browser session and profile at /api/auth. This prefix stays unversioned because " +
        "it is the OIDC login and logout entry, not a versioned business resource. " +
        "GET /login redirects to the configured identity provider. GET /session reports whether " +
        "a session cookie is present. GET /profile returns the local profile after identity fields " +
        "are mirrored from the provider. PUT /profile updates display name, email, and avatar URL " +
        "in the provider, then stores phone, description, occupation, homepage, and the remaining " +
        "personal details in AgentGo. Avatar bytes are uploaded separately at PUT /api/v1/files/avatar. " +
        "Missing authentication uses AUTH-002.",
)
@RequestMapping("/api/auth")
interface AuthControllerApiDoc {
    @Operation(summary = "Start OIDC login", description = "Redirects the browser to the configured OIDC provider.")
    @ApiResponse(responseCode = "302", description = "Redirect to the OIDC authorization endpoint")
    @GetMapping("/login")
    fun login(response: HttpServletResponse)

    @Operation(summary = "Get current session", description = "Returns the current authenticated user session.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Authenticated session", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "No authenticated session", useReturnTypeSchema = true),
        ],
    )
    @GetMapping("/session")
    fun session(): ResponseEntity<SessionResponse>

    @Operation(summary = "Get current user profile", description = "Returns the current profile. Identity fields are synchronized from Authentik and mirrored locally. Extended personal details are read from AgentGo.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Current user profile", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "No authenticated session"),
        ],
    )
    @GetMapping("/profile")
    fun profile(): ResponseEntity<ProfileResponse>

    @Operation(summary = "Update current user profile", description = "Updates display name, email, and avatar URL in Authentik, then stores the full profile, including phone, description, occupation, homepage, and other personal details, in AgentGo.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Profile updated", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid profile data"),
            ApiResponse(responseCode = "401", description = "No authenticated session"),
        ],
    )
    @PutMapping("/profile")
    fun updateProfile(@RequestBody request: ProfileUpdateRequest): ResponseEntity<ProfileResponse>
}
