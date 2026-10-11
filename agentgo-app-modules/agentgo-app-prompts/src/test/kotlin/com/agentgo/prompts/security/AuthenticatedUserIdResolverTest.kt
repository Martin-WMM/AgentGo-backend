package com.agentgo.prompts.security

import com.agentgo.prompts.exception.PromptTemplateException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.springframework.http.HttpStatus
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
    fun `returns authenticated principal name`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("ada", "n/a", emptyList())

        assertEquals("ada", resolver.requireUserId())
    }

    @Test
    fun `rejects missing authentication`() {
        SecurityContextHolder.clearContext()

        val exception = assertFailsWith<PromptTemplateException> { resolver.requireUserId() }

        assertEquals(1002, exception.code)
        assertEquals("AUTH-002", exception.responseType)
    }

    @Test
    fun `rejects anonymous authentication`() {
        SecurityContextHolder.getContext().authentication =
            AnonymousAuthenticationToken("key", "anonymousUser", listOf(SimpleGrantedAuthority("ROLE_ANONYMOUS")))

        val exception = assertFailsWith<PromptTemplateException> { resolver.requireUserId() }

        assertEquals("AUTH-002", exception.responseType)
    }

    @Test
    fun `rejects blank principal name`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(" ", "n/a", emptyList())

        val exception = assertFailsWith<PromptTemplateException> { resolver.requireUserId() }

        assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
    }
}
