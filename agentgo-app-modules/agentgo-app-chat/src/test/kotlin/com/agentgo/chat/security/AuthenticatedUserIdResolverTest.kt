package com.agentgo.chat.security

import com.agentgo.chat.exception.ChatException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

class AuthenticatedUserIdResolverTest {
    private val resolver = AuthenticatedUserIdResolver()

    @Test
    fun `requireUserId returns the principal name`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("ada", "n/a", AuthorityUtils.NO_AUTHORITIES)

        assertEquals("ada", resolver.requireUserId())
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `requireUserId rejects anonymous authentication`() {
        SecurityContextHolder.getContext().authentication =
            AnonymousAuthenticationToken("key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"))

        assertFailsWith<ChatException> { resolver.requireUserId() }
        SecurityContextHolder.clearContext()
    }
}
