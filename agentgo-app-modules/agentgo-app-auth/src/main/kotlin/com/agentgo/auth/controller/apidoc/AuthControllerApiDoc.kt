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

@Tag(name = "Authentication", description = "AgentGo authentication and session endpoints")
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

    @Operation(summary = "Get current user profile", description = "Returns the current profile synchronized from Authentik.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Current user profile", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "No authenticated session"),
        ],
    )
    @GetMapping("/profile")
    fun profile(): ResponseEntity<ProfileResponse>

    @Operation(summary = "Update current user profile", description = "Updates the current user's profile in Authentik.")
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
