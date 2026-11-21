package com.agentgo.auth.service.impl

import com.agentgo.auth.client.AuthentikProfileClient
import com.agentgo.auth.client.AuthentikUserResponse
import com.agentgo.auth.entity.UserProfileEntity
import com.agentgo.auth.exception.InvalidProfileRequestException
import com.agentgo.auth.mapper.UserProfileMapperImpl
import com.agentgo.auth.repository.UserProfileRepository
import com.agentgo.auth.support.NoOpPlatformTransactionManager
import com.agentgo.dto.auth.ProfileUpdateRequest
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UserProfileServiceImplTest {
    private val authentikProfileClient = mock<AuthentikProfileClient>()
    private val userProfileRepository = mock<UserProfileRepository>()
    private val service = UserProfileServiceImpl(
        authentikProfileClient,
        userProfileRepository,
        UserProfileMapperImpl(),
        NoOpPlatformTransactionManager(),
    )

    @Test
    fun `current profile creates a local snapshot from authentik`() {
        whenever(authentikProfileClient.findBySubject("subject-ada", "ada@example.com")).thenReturn(authentikUser())
        whenever(userProfileRepository.findByOwnerUserId("subject-ada")).thenReturn(null)
        whenever(userProfileRepository.save(any())).thenAnswer { it.arguments[0] }

        val profile = service.currentProfile("subject-ada", "ada@example.com")

        assertEquals("ada", profile.username)
        assertEquals("Ada Lovelace", profile.displayName)
        assertEquals("ada@example.com", profile.email)
        assertEquals("https://example.com/avatar.png", profile.avatarUrl)
        assertNull(profile.phoneNumber)
        assertNull(profile.occupation)
    }

    @Test
    fun `current profile refreshes identity and keeps stored personal details`() {
        val existing = UserProfileEntity(
            ownerUserId = "subject-ada",
            username = "ada",
            displayName = "Old Name",
            phoneNumber = "+1 202 555 0100",
            occupation = "Mathematician",
            homepageUrl = "https://example.com/ada",
        )
        whenever(authentikProfileClient.findBySubject("subject-ada", null)).thenReturn(
            authentikUser(name = "Ada Lovelace"),
        )
        whenever(userProfileRepository.findByOwnerUserId("subject-ada")).thenReturn(existing)
        whenever(userProfileRepository.save(any())).thenAnswer { it.arguments[0] }

        val profile = service.currentProfile("subject-ada", null)

        assertEquals("Ada Lovelace", profile.displayName)
        assertEquals("+1 202 555 0100", profile.phoneNumber)
        assertEquals("Mathematician", profile.occupation)
        assertEquals("https://example.com/ada", profile.homepageUrl)
    }

    @Test
    fun `current profile keeps AgentGo avatar when Authentik only has a data image`() {
        val existing = UserProfileEntity(
            ownerUserId = "subject-ada",
            username = "ada",
            displayName = "Ada",
            avatarUrl = "http://localhost:8080/api/files/avatar",
        )
        whenever(authentikProfileClient.findBySubject("subject-ada", null)).thenReturn(
            authentikUser(avatar = "data:image/svg+xml;base64,AAAA"),
        )
        whenever(userProfileRepository.findByOwnerUserId("subject-ada")).thenReturn(existing)
        whenever(userProfileRepository.save(any())).thenAnswer { it.arguments[0] }

        val profile = service.currentProfile("subject-ada", null)

        assertEquals("http://localhost:8080/api/files/avatar", profile.avatarUrl)
    }

    @Test
    fun `update profile stores AgentGo avatar without requiring Authentik avatar sync`() {
        whenever(authentikProfileClient.findBySubject("subject-ada", null)).thenReturn(
            authentikUser(avatar = "data:image/svg+xml;base64,AAAA"),
        )
        whenever(authentikProfileClient.update(any(), any())).thenReturn(
            authentikUser(avatar = "data:image/svg+xml;base64,AAAA"),
        )
        whenever(userProfileRepository.findByOwnerUserId("subject-ada")).thenReturn(null)
        whenever(userProfileRepository.save(any())).thenAnswer { it.arguments[0] }

        val profile = service.updateProfile(
            "subject-ada",
            null,
            ProfileUpdateRequest().apply {
                displayName = "Ada Lovelace"
                email = "ada@example.com"
                avatarUrl = "http://localhost:8080/api/files/avatar"
            },
        )

        assertEquals("http://localhost:8080/api/files/avatar", profile.avatarUrl)
        verify(authentikProfileClient).update(any(), any())
    }

    @Test
    fun `update profile writes identity to authentik and stores personal details`() {
        whenever(authentikProfileClient.findBySubject("subject-ada", null)).thenReturn(authentikUser())
        whenever(authentikProfileClient.update(any(), any())).thenReturn(
            authentikUser(
                name = "Ada Lovelace",
                email = "ada@example.com",
                avatar = "https://example.com/avatar.png",
            ),
        )
        whenever(userProfileRepository.findByOwnerUserId("subject-ada")).thenReturn(null)
        whenever(userProfileRepository.save(any())).thenAnswer { it.arguments[0] }

        val profile = service.updateProfile(
            "subject-ada",
            null,
            ProfileUpdateRequest().apply {
                displayName = " Ada Lovelace "
                email = " ada@example.com "
                avatarUrl = " https://example.com/avatar.png "
                phoneNumber = " +86 138 0013 8000 "
                description = " Analytical engines "
                occupation = " Mathematician "
                organization = " Analytical Engines "
                homepageUrl = " https://example.com/ada "
                location = " London "
                timezone = " Europe/London "
                preferredLanguage = " en "
            },
        )

        assertEquals("Ada Lovelace", profile.displayName)
        assertEquals("ada@example.com", profile.email)
        assertEquals("+86 138 0013 8000", profile.phoneNumber)
        assertEquals("Analytical engines", profile.description)
        assertEquals("Mathematician", profile.occupation)
        assertEquals("Analytical Engines", profile.organization)
        assertEquals("https://example.com/ada", profile.homepageUrl)
        assertEquals("London", profile.location)
        assertEquals("Europe/London", profile.timezone)
        assertEquals("en", profile.preferredLanguage)
        verify(authentikProfileClient).update(any(), any())
    }

    @Test
    fun `update profile clears blank personal details`() {
        val existing = UserProfileEntity(
            ownerUserId = "subject-ada",
            username = "ada",
            displayName = "Ada",
            phoneNumber = "+1 202 555 0100",
            occupation = "Mathematician",
        )
        whenever(authentikProfileClient.findBySubject("subject-ada", null)).thenReturn(authentikUser())
        whenever(authentikProfileClient.update(any(), any())).thenReturn(authentikUser(avatar = null))
        whenever(userProfileRepository.findByOwnerUserId("subject-ada")).thenReturn(existing)
        whenever(userProfileRepository.save(any())).thenAnswer { it.arguments[0] }

        val profile = service.updateProfile(
            "subject-ada",
            null,
            ProfileUpdateRequest().apply {
                displayName = "Ada"
                email = "ada@example.com"
                avatarUrl = " "
                phoneNumber = " "
                occupation = " "
            },
        )

        assertNull(profile.phoneNumber)
        assertNull(profile.occupation)
        assertNull(profile.avatarUrl)
    }

    @Test
    fun `update profile rejects a blank display name before calling authentik`() {
        val exception = assertFailsWith<InvalidProfileRequestException> {
            service.updateProfile(
                "subject-ada",
                null,
                ProfileUpdateRequest().apply { displayName = " " },
            )
        }

        assertEquals("profile.display-name.required", exception.messageCode)
        verify(authentikProfileClient, never()).findBySubject(any(), anyOrNull())
        verify(authentikProfileClient, never()).update(any(), any())
    }

    @Test
    fun `update profile rejects an unknown time zone`() {
        val exception = assertFailsWith<InvalidProfileRequestException> {
            service.updateProfile(
                "subject-ada",
                null,
                ProfileUpdateRequest().apply {
                    displayName = "Ada"
                    timezone = "Not/AZone"
                },
            )
        }

        assertEquals("profile.timezone.invalid", exception.messageCode)
        verify(userProfileRepository, never()).save(any())
    }

    @Test
    fun `update profile rejects a homepage that is not an http url`() {
        val exception = assertFailsWith<InvalidProfileRequestException> {
            service.updateProfile(
                "subject-ada",
                null,
                ProfileUpdateRequest().apply {
                    displayName = "Ada"
                    homepageUrl = "javascript:alert(1)"
                },
            )
        }

        assertEquals("profile.homepage.invalid", exception.messageCode)
    }

    private fun authentikUser(
        name: String = "Ada Lovelace",
        email: String? = "ada@example.com",
        avatar: String? = "https://example.com/avatar.png",
    ) = AuthentikUserResponse(
        pk = 7,
        username = "ada",
        name = name,
        email = email,
        avatar = avatar,
        uuid = UUID.fromString("11111111-1111-1111-1111-111111111111"),
    )
}
