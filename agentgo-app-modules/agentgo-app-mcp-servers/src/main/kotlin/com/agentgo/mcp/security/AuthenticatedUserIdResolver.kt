package com.agentgo.mcp.security

import com.agentgo.mcp.exception.McpException
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

/**
 * Resolves the authenticated user identifier from the Spring Security context.
 *
 * MCP APIs never accept an owner identifier from the client. Ownership is always
 * derived from the authenticated principal name configured by the OIDC login flow.
 */
@Component
class AuthenticatedUserIdResolver {
    /**
     * Returns the authenticated principal name.
     *
     * @return non-blank owner identifier
     * @throws McpException when authentication is missing or anonymous
     */
    fun requireUserId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (
            authentication == null ||
            !authentication.isAuthenticated ||
            authentication is AnonymousAuthenticationToken ||
            authentication.name.isNullOrBlank()
        ) {
            throw McpException(
                code = 1002,
                responseType = "AUTH-002",
                status = HttpStatus.UNAUTHORIZED,
                message = "Authentication is required.",
            )
        }
        return authentication.name
    }
}
