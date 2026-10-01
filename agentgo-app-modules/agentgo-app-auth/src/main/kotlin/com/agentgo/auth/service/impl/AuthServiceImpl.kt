package com.agentgo.auth.service.impl

import com.agentgo.auth.client.AuthentikProfileClient
import com.agentgo.auth.service.AuthService
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
        val user = authentikProfileClient.findBySubject(session.subject!!, session.email)
        return user.toProfileResponse()
    }

    override fun updateProfile(request: ProfileUpdateRequest): ProfileResponse {
        require(request.displayName.isNotBlank()) { "Display name must not be blank" }
        val session = requireAuthenticatedSession()
        val user = authentikProfileClient.findBySubject(session.subject!!, session.email)
        return authentikProfileClient.update(user, request).toProfileResponse()
    }

    private fun requireAuthenticatedSession(): SessionResponse {
        val session = currentSession()
        check(session.authenticated && !session.subject.isNullOrBlank()) {
            "An authenticated OIDC session is required"
        }
        return session
    }

    private fun com.agentgo.auth.client.AuthentikUserResponse.toProfileResponse() = ProfileResponse(
        username = username,
        displayName = name,
        email = email,
        avatarUrl = avatar,
    )
}
