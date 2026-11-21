package com.agentgo.auth.service.impl

import com.agentgo.auth.client.AuthentikProfileClient
import com.agentgo.auth.service.UserProfileService
import com.agentgo.dto.auth.ProfileResponse
import com.agentgo.dto.auth.ProfileUpdateRequest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class AuthServiceImplTest {
    private val authentikProfileClient = mock<AuthentikProfileClient>()
    private val userProfileService = mock<UserProfileService>()
    private val service = AuthServiceImpl(authentikProfileClient, userProfileService)

    @AfterTest
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `current profile requires an authenticated session`() {
        assertFailsWith<IllegalStateException> { service.currentProfile() }
        verify(userProfileService, never()).currentProfile(org.mockito.kotlin.any(), org.mockito.kotlin.anyOrNull())
    }

    @Test
    fun `current profile delegates the authenticated subject`() {
        authenticate("subject-ada")
        val profile = ProfileResponse("ada", "Ada", "ada@example.com", null)
        whenever(userProfileService.currentProfile("subject-ada", null)).thenReturn(profile)

        assertEquals(profile, service.currentProfile())
    }

    @Test
    fun `update profile delegates the authenticated subject and request`() {
        authenticate("subject-ada")
        val request = ProfileUpdateRequest().apply {
            displayName = "Ada"
            email = "ada@example.com"
            occupation = "Mathematician"
        }
        val profile = ProfileResponse("ada", "Ada", "ada@example.com", null, occupation = "Mathematician")
        whenever(userProfileService.updateProfile("subject-ada", null, request)).thenReturn(profile)

        assertEquals(profile, service.updateProfile(request))
        verify(userProfileService).updateProfile("subject-ada", null, request)
    }

    private fun authenticate(subject: String) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(subject, "n/a", emptyList())
    }
}
