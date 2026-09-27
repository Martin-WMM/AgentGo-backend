package com.agentgo.auth.controller

import com.agentgo.auth.properties.AuthProperties
import com.agentgo.auth.controller.apidoc.AuthControllerApiDoc
import com.agentgo.auth.service.AuthService
import com.agentgo.dto.auth.SessionResponse
import com.agentgo.dto.auth.ProfileResponse
import com.agentgo.dto.auth.ProfileUpdateRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authProperties: AuthProperties,
    private val authService: AuthService,
) : AuthControllerApiDoc {
    override fun login(response: HttpServletResponse) {
        response.sendRedirect("/oauth2/authorization/${authProperties.registrationId}")
    }

    override fun session(): ResponseEntity<SessionResponse> {
        val session = authService.currentSession()
        return if (session.authenticated) {
            ResponseEntity.ok(session)
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(session)
        }
    }

    override fun profile(): ResponseEntity<ProfileResponse> = ResponseEntity.ok(authService.currentProfile())

    override fun updateProfile(request: ProfileUpdateRequest): ResponseEntity<ProfileResponse> =
        ResponseEntity.ok(authService.updateProfile(request))
}
