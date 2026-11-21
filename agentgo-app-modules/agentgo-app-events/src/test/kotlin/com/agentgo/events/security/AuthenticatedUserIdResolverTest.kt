package com.agentgo.events.security

import com.agentgo.events.exception.SystemEventException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

class AuthenticatedUserIdResolverTest {
    private val resolver = AuthenticatedUserIdResolver()

    @AfterTest
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `returns the authenticated principal name`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("ada", "n/a", emptyList())

        assertEquals("ada", resolver.requireUserId())
    }

    @Test
    fun `rejects missing authentication`() {
        SecurityContextHolder.clearContext()

        val exception = assertFailsWith<SystemEventException> { resolver.requireUserId() }

        assertEquals(1002, exception.code)
        assertEquals("AUTH-002", exception.responseType)
    }

    @Test
    fun `rejects anonymous authentication`() {
        SecurityContextHolder.getContext().authentication = AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            listOf(SimpleGrantedAuthority("ROLE_ANONYMOUS")),
        )

        val exception = assertFailsWith<SystemEventException> { resolver.requireUserId() }

        assertEquals(HttpStatusUnauthorized, exception.status.value())
    }

    @Test
    fun `rejects a blank principal name`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("  ", "n/a", emptyList())

        val exception = assertFailsWith<SystemEventException> { resolver.requireUserId() }

        assertEquals("AUTH-002", exception.responseType)
    }

    private companion object {
        const val HttpStatusUnauthorized = 401
    }
}
