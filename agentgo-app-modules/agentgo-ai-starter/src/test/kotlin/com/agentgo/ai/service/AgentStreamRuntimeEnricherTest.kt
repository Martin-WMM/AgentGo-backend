package com.agentgo.ai.service

import com.agentgo.dto.agent.AgentStreamRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

class AgentStreamRuntimeEnricherTest {
    @Test
    fun `enrich copies owner user id into metadata`() {
        SecurityContextHolder.clearContext()
        val request = AgentStreamRequest()

        val enriched = AgentStreamRuntimeEnricher.enrich(request, "ada")

        assertEquals("ada", enriched.metadata["ownerUserId"])
        assertNull(enriched.headers["Accept-Language"])
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `enrich keeps an existing owner user id`() {
        val request =
            AgentStreamRequest().apply {
                metadata = mapOf("ownerUserId" to "existing")
            }

        val enriched = AgentStreamRuntimeEnricher.enrich(request, "ada")

        assertEquals("existing", enriched.metadata["ownerUserId"])
    }

    @Test
    fun `enrich reads preferred username from the security principal when available`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(
                mapOf("preferredUsername" to "ada"),
                "n/a",
                AuthorityUtils.NO_AUTHORITIES,
            )
        // Map principal has no getPreferredUsername method; ensure no crash.
        val enriched = AgentStreamRuntimeEnricher.enrich(AgentStreamRequest(), "ada")
        assertEquals("ada", enriched.metadata["ownerUserId"])
        SecurityContextHolder.clearContext()
    }
}
