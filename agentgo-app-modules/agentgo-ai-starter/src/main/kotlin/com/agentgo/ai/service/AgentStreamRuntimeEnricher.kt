package com.agentgo.ai.service

import com.agentgo.ai.service.impl.AgentStreamServiceImpl
import com.agentgo.dto.agent.AgentStreamRequest
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * Copies HTTP / security identity into the agent stream request so tool executors
 * (especially Bash / CLI) can inject runtime context into process environments.
 */
object AgentStreamRuntimeEnricher {
    const val ID_TOKEN_METADATA: String = "idToken"
    const val USERNAME_METADATA: String = "username"

    /**
     * Returns a copy of [request] with owner, locale, authorization, and OIDC
     * identity fields filled from the current servlet request and security context.
     *
     * @param request original stream body
     * @param ownerUserId authenticated subject, when known
     * @return enriched request used for the runtime command
     */
    fun enrich(request: AgentStreamRequest, ownerUserId: String?): AgentStreamRequest {
        val headers = request.headers.toMutableMap()
        val metadata = request.metadata.toMutableMap()
        val httpRequest = currentHttpRequest()
        if (httpRequest != null) {
            copyHeader(httpRequest, headers, "Accept-Language")
            copyHeader(httpRequest, headers, "Authorization")
        }
        if (!ownerUserId.isNullOrBlank()) {
            metadata.putIfAbsent(AgentStreamServiceImpl.OWNER_USER_ID_METADATA, ownerUserId)
        }
        val principal = currentPrincipal()
        if (principal != null) {
            maybeStringProperty(principal, "getPreferredUsername")?.let {
                metadata.putIfAbsent(USERNAME_METADATA, it)
            }
            maybeIdToken(principal)?.let { metadata.putIfAbsent(ID_TOKEN_METADATA, it) }
        }
        return AgentStreamRequest().apply {
            state = request.state
            config = request.config
            this.headers = headers
            this.metadata = metadata
        }
    }

    private fun currentHttpRequest(): HttpServletRequest? {
        val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        return attributes?.request
    }

    private fun currentPrincipal(): Any? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (
            authentication == null ||
            !authentication.isAuthenticated ||
            authentication is AnonymousAuthenticationToken
        ) {
            return null
        }
        return authentication.principal
    }

    private fun copyHeader(
        httpRequest: HttpServletRequest,
        headers: MutableMap<String, String>,
        name: String,
    ) {
        if (headers.keys.any { it.equals(name, ignoreCase = true) }) {
            return
        }
        val value = httpRequest.getHeader(name)?.trim().orEmpty()
        if (value.isNotEmpty()) {
            headers[name] = value
        }
    }

    private fun maybeIdToken(principal: Any): String? {
        val idToken = maybeInvoke(principal, "getIdToken") ?: return null
        return maybeStringProperty(idToken, "getTokenValue")
    }

    private fun maybeStringProperty(target: Any, methodName: String): String? {
        val value = maybeInvoke(target, methodName) as? String ?: return null
        return value.trim().takeIf { it.isNotEmpty() }
    }

    private fun maybeInvoke(target: Any, methodName: String): Any? =
        runCatching {
            target.javaClass.methods
                .firstOrNull { it.name == methodName && it.parameterCount == 0 }
                ?.invoke(target)
        }.getOrNull()
}
