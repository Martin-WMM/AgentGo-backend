package com.agentgo.settings.service.impl

import com.agentgo.dto.settings.UiDensity
import com.agentgo.dto.settings.UiLocale
import com.agentgo.dto.settings.UiTheme
import com.agentgo.dto.settings.UserSettingsUpdateRequest
import com.agentgo.settings.entity.UserSettingsEntity
import com.agentgo.settings.exception.SettingsException
import com.agentgo.settings.mapper.UserSettingsMapperImpl
import com.agentgo.settings.repository.UserSettingsRepository
import com.agentgo.settings.security.SettingsUserIdResolver
import com.agentgo.settings.support.NoOpPlatformTransactionManager
import java.time.Instant
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

class UserSettingsServiceImplTest {
    private val userSettingsRepository = mock<UserSettingsRepository>()
    private val service = UserSettingsServiceImpl(
        userSettingsRepository,
        UserSettingsMapperImpl(),
        SettingsUserIdResolver(),
        NoOpPlatformTransactionManager(),
    )

    @AfterTest
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `get returns defaults without writing when the user has no row`() {
        authenticateAs("ada")
        whenever(userSettingsRepository.findByOwnerUserId("ada")).thenReturn(null)

        val response = service.get()

        assertEquals(UiTheme.SYSTEM, response.theme)
        assertEquals(UiLocale.EN, response.locale)
        assertFalse(response.persisted)
        verify(userSettingsRepository, never()).save(any())
    }

    @Test
    fun `get returns the stored row for the authenticated owner`() {
        authenticateAs("ada")
        whenever(userSettingsRepository.findByOwnerUserId("ada")).thenReturn(stored(theme = UiTheme.LIGHT))

        val response = service.get()

        assertEquals(UiTheme.LIGHT, response.theme)
        assertTrue(response.persisted)
        assertEquals(Instant.parse("2026-10-11T08:00:00Z"), response.updatedAt)
    }

    @Test
    fun `update inserts settings for a user who has none`() {
        authenticateAs("ada")
        whenever(userSettingsRepository.findByOwnerUserId("ada")).thenReturn(null)
        whenever(userSettingsRepository.save(any<UserSettingsEntity>())).thenAnswer { invocation ->
            invocation.getArgument<UserSettingsEntity>(0).apply {
                id = UUID.randomUUID()
                updatedAt = Instant.parse("2026-10-11T09:00:00Z")
            }
