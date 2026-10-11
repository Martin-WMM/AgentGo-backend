package com.agentgo.auth.service.impl

import com.agentgo.auth.client.AuthentikProfileClient
import com.agentgo.auth.service.AuthService
import com.agentgo.auth.service.UserProfileService
import com.agentgo.dto.auth.ProfileResponse
import com.agentgo.dto.auth.ProfileUpdateRequest
import com.agentgo.dto.auth.SessionResponse
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service

@Service
class AuthServiceImpl(
    private val authentikProfileClient: AuthentikProfileClient,
    private val userProfileService: UserProfileService,
) : AuthService {
    override fun currentSession(): SessionResponse {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null ||
            !authentication.isAuthenticated ||
            authentication is AnonymousAuthenticationToken
        ) {
            return SessionResponse(authenticated = false)
        }

        val oidcUser = authentication.principal as? OidcUser
        val sessionId = oidcUser?.getClaimAsString("sid")
        if (!sessionId.isNullOrBlank() && !authentikProfileClient.isSessionActive(sessionId)) {
            SecurityContextHolder.clearContext()
            return SessionResponse(authenticated = false)
        }
        return SessionResponse(
            authenticated = true,
            subject = oidcUser?.subject ?: authentication.name,
            name = oidcUser?.fullName ?: oidcUser?.preferredUsername ?: authentication.name,
            email = oidcUser?.email,
            picture = oidcUser?.getClaimAsString("picture"),
        )
    }

    override fun currentProfile(): ProfileResponse {
        val session = requireAuthenticatedSession()
        return userProfileService.currentProfile(session.subject!!, session.email)
    }

    override fun updateProfile(request: ProfileUpdateRequest): ProfileResponse {
        val session = requireAuthenticatedSession()
        return userProfileService.updateProfile(session.subject!!, session.email, request)
    }

    private fun requireAuthenticatedSession(): SessionResponse {
        val session = currentSession()
        check(session.authenticated && !session.subject.isNullOrBlank()) {
            "An authenticated OIDC session is required"
        }
        return session
    }
}
