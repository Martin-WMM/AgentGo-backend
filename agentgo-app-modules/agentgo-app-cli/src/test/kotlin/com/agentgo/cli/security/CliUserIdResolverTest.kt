package com.agentgo.cli.security

import com.agentgo.cli.exception.CliException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

class CliUserIdResolverTest {
    private val resolver = CliUserIdResolver()

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

        val exception = assertFailsWith<CliException> { resolver.requireUserId() }

        assertEquals(1002, exception.code)
        assertEquals("AUTH-002", exception.responseType)
        assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
    }

    @Test
    fun `rejects anonymous authentication`() {
        SecurityContextHolder.getContext().authentication = AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            listOf(SimpleGrantedAuthority("ROLE_ANONYMOUS")),
        )

        val exception = assertFailsWith<CliException> { resolver.requireUserId() }

        assertEquals("AUTH-002", exception.responseType)
        assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
    }

    @Test
    fun `rejects a blank principal name`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("  ", "n/a", emptyList())

        val exception = assertFailsWith<CliException> { resolver.requireUserId() }

        assertEquals("AUTH-002", exception.responseType)
    }

    @Test
    fun `rejects an unauthenticated token`() {
        val token = UsernamePasswordAuthenticationToken("ada", "n/a")
        SecurityContextHolder.getContext().authentication = token

        val exception = assertFailsWith<CliException> { resolver.requireUserId() }

        assertEquals(1002, exception.code)
    }
}
