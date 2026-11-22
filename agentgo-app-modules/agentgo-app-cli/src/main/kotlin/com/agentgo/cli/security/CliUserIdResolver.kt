package com.agentgo.cli.security

import com.agentgo.cli.exception.CliException
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

/**
 * Resolves the authenticated user identifier from the Spring Security context.
 *
 * CLI APIs never accept an operator identifier from the client. Ownership is always
 * derived from the authenticated principal name configured by the OIDC login flow.
 */
@Component
class CliUserIdResolver {
    /**
     * Returns the authenticated principal name.
     *
     * @return non-blank owner identifier
     * @throws CliException when authentication is missing or anonymous
     */
    fun requireUserId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (
            authentication == null ||
            !authentication.isAuthenticated ||
            authentication is AnonymousAuthenticationToken ||
            authentication.name.isNullOrBlank()
        ) {
            throw CliException(
                code = 1002,
                responseType = "AUTH-002",
                status = HttpStatus.UNAUTHORIZED,
                message = "Authentication is required.",
            )
        }
        return authentication.name
    }
}
