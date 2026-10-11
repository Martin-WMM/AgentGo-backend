package com.agentgo.auth.controller

import com.agentgo.auth.properties.AuthProperties
import com.agentgo.auth.service.AuthService
import com.agentgo.dto.auth.ProfileResponse
import com.agentgo.dto.auth.ProfileUpdateRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AuthControllerTest {
    private val authService = mock<AuthService>()
    private val controller = AuthController(AuthProperties(), authService)

    @Test
    fun `profile returns the current merged profile`() {
        val profile = ProfileResponse(
            username = "ada",
            displayName = "Ada Lovelace",
            email = "ada@example.com",
            avatarUrl = null,
            occupation = "Mathematician",
        )
        whenever(authService.currentProfile()).thenReturn(profile)

        val response = controller.profile()

        assertEquals(200, response.statusCode.value())
        assertEquals(profile, response.body)
    }

    @Test
    fun `update profile delegates the request and returns the saved profile`() {
        val request = ProfileUpdateRequest().apply {
            displayName = "Ada Lovelace"
            email = "ada@example.com"
            avatarUrl = null
            homepageUrl = "https://example.com/ada"
        }
        val profile = ProfileResponse(
            username = "ada",
            displayName = "Ada Lovelace",
            email = "ada@example.com",
            avatarUrl = null,
            homepageUrl = "https://example.com/ada",
        )
        whenever(authService.updateProfile(request)).thenReturn(profile)

        val response = controller.updateProfile(request)

        assertEquals(200, response.statusCode.value())
        assertEquals(profile, response.body)
        verify(authService).updateProfile(request)
    }
}
