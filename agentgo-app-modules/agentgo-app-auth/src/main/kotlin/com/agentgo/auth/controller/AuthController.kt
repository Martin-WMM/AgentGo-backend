package com.agentgo.auth.controller

import com.agentgo.auth.properties.AuthProperties
import com.agentgo.auth.controller.apidoc.AuthControllerApiDoc
import com.agentgo.auth.service.AuthService
import com.agentgo.commons.annotation.AgentGoLogApi
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
    @AgentGoLogApi(name = "auth.login")
    override fun login(response: HttpServletResponse) {
        response.sendRedirect("/oauth2/authorization/${authProperties.registrationId}")
    }

    @AgentGoLogApi(name = "auth.session")
    override fun session(): ResponseEntity<SessionResponse> {
        val session = authService.currentSession()
        return if (session.authenticated) {
            ResponseEntity.ok(session)
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(session)
        }
    }

    @AgentGoLogApi(name = "auth.profile.get")
    override fun profile(): ResponseEntity<ProfileResponse> = ResponseEntity.ok(authService.currentProfile())

    @AgentGoLogApi(name = "auth.profile.update")
    override fun updateProfile(request: ProfileUpdateRequest): ResponseEntity<ProfileResponse> =
        ResponseEntity.ok(authService.updateProfile(request))
}
