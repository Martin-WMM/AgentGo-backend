package com.agentgo.models.security

import com.agentgo.models.exception.ModelException
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
    fun returnsAuthenticatedPrincipalName() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("ada", "n/a", emptyList())

        assertEquals("ada", resolver.requireUserId())
    }

    @Test
    fun rejectsMissingAuthentication() {
        SecurityContextHolder.clearContext()

        val exception = assertFailsWith<ModelException> { resolver.requireUserId() }

        assertEquals(1002, exception.code)
        assertEquals("AUTH-002", exception.responseType)
    }

    @Test
    fun rejectsAnonymousAuthentication() {
        SecurityContextHolder.getContext().authentication =
            AnonymousAuthenticationToken("key", "anonymousUser", listOf(SimpleGrantedAuthority("ROLE_ANONYMOUS")))

        val exception = assertFailsWith<ModelException> { resolver.requireUserId() }

        assertEquals("AUTH-002", exception.responseType)
    }
}
