package com.agentgo.file.config

import java.util.Optional
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

class JpaAuditingConfigurationTest {
    private val auditor = JpaAuditingConfiguration().agentGoAuditorAware()

    @AfterTest
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `reads creator and updatedBy from the current authentication name`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("ada", "n/a", AuthorityUtils.NO_AUTHORITIES)

        assertEquals(Optional.of("ada"), auditor.currentAuditor)
    }

    @Test
    fun `rejects a missing authentication`() {
        val failure = assertFailsWith<IllegalStateException> { auditor.currentAuditor }

        assertEquals("Audit fields require an authenticated user.", failure.message)
    }

    @Test
    fun `rejects an anonymous authentication`() {
        SecurityContextHolder.getContext().authentication = AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"),
        )

        assertFailsWith<IllegalStateException> { auditor.currentAuditor }
    }

    @Test
    fun `rejects a blank authentication name`() {
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
            " ",
            "n/a",
            AuthorityUtils.NO_AUTHORITIES,
        )

        assertFailsWith<IllegalStateException> { auditor.currentAuditor }
    }
}
