package com.agentgo.ai.tool

import com.agentgo.ai.service.impl.AgentStreamServiceImpl
import com.agentgo.core.tool.ToolRuntimeContext
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Ensures CLI / Bash tool calls see an authenticated SecurityContext.
 *
 * Agent graph nodes may run on worker threads that lost the HTTP request context.
 * When that happens, restore a principal from [ToolRuntimeContext] metadata.
 */
internal object BashToolSecuritySupport {
    /**
     * Runs [block] with a SecurityContext that can satisfy CLI ownership checks.
     *
     * @param context tool runtime context carrying `ownerUserId`
     * @param block CLI execution body
     * @return result of [block]
     */
    fun <T> withAuthenticatedContext(context: ToolRuntimeContext, block: () -> T): T {
        if (hasAuthenticatedPrincipal()) {
            return block()
        }
        val ownerUserId =
            context.metadata[AgentStreamServiceImpl.OWNER_USER_ID_METADATA]
                ?.trim()
                .orEmpty()
                .ifBlank {
                    context.metadata["userId"]?.trim().orEmpty()
                }
        if (ownerUserId.isBlank()) {
            return block()
        }

        val previous = SecurityContextHolder.getContext()
        val restored = SecurityContextHolder.createEmptyContext()
        restored.authentication =
            UsernamePasswordAuthenticationToken(
                ownerUserId,
                "n/a",
                AuthorityUtils.NO_AUTHORITIES,
            )
        SecurityContextHolder.setContext(restored)
        return try {
            block()
        } finally {
            restore(previous)
        }
    }

    private fun hasAuthenticatedPrincipal(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication != null &&
            authentication.isAuthenticated &&
            authentication !is AnonymousAuthenticationToken &&
            !authentication.name.isNullOrBlank()
    }

    private fun restore(previous: SecurityContext) {
        if (previous.authentication == null) {
            SecurityContextHolder.clearContext()
        } else {
            SecurityContextHolder.setContext(previous)
        }
    }
}
