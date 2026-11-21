package com.agentgo.events.security

import com.agentgo.events.exception.SystemEventException
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

/**
 * Resolves the authenticated user identifier from the Spring Security context.
 *
 * System events are global, but every write and read still requires an authenticated
 * principal. A blank operator on a write falls back to this identifier.
 */
@Component("systemEventUserIdResolver")
class AuthenticatedUserIdResolver {
    /**
     * Returns the authenticated principal name.
     *
     * @return non-blank user identifier
     * @throws SystemEventException when authentication is missing or anonymous
     */
    fun requireUserId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (
            authentication == null ||
            !authentication.isAuthenticated ||
            authentication is AnonymousAuthenticationToken ||
            authentication.name.isNullOrBlank()
        ) {
            throw SystemEventException(
                code = 1002,
                responseType = "AUTH-002",
                status = HttpStatus.UNAUTHORIZED,
                message = "Authentication is required.",
            )
        }
        return authentication.name
    }
}
