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
        }

        val response = service.update(request(theme = UiTheme.DARK, sidebarCollapsed = true))

        assertEquals(UiTheme.DARK, response.theme)
        assertTrue(response.sidebarCollapsed)
        assertTrue(response.persisted)
        verify(userSettingsRepository).save(any())
    }

    @Test
    fun `update replaces the existing row`() {
        authenticateAs("ada")
        val existing = stored(theme = UiTheme.LIGHT)
        whenever(userSettingsRepository.findByOwnerUserId("ada")).thenReturn(existing)
        whenever(userSettingsRepository.save(existing)).thenReturn(existing)

        val response = service.update(request(theme = UiTheme.DARK, locale = UiLocale.ZH_CN))

        assertEquals(UiTheme.DARK, existing.theme)
        assertEquals(UiLocale.ZH_CN, existing.locale)
        assertEquals(UiTheme.DARK, response.theme)
        assertEquals("ada", existing.ownerUserId)
    }

    @Test
    fun `update retries as a replacement when the first insert loses the unique owner race`() {
        authenticateAs("ada")
        val existing = stored(theme = UiTheme.SYSTEM)
        whenever(userSettingsRepository.findByOwnerUserId("ada")).thenReturn(null, existing)
        whenever(userSettingsRepository.save(any<UserSettingsEntity>()))
            .thenThrow(DataIntegrityViolationException("duplicate owner"))
            .thenReturn(existing)

        val response = service.update(request(theme = UiTheme.LIGHT, reduceMotion = true))

        assertEquals(UiTheme.LIGHT, existing.theme)
        assertTrue(existing.reduceMotion)
        assertEquals(UiTheme.LIGHT, response.theme)
    }

    @Test
    fun `update rethrows the conflict when the raced row is still missing`() {
        authenticateAs("ada")
        whenever(userSettingsRepository.findByOwnerUserId("ada")).thenReturn(null)
        whenever(userSettingsRepository.save(any<UserSettingsEntity>()))
            .thenThrow(DataIntegrityViolationException("duplicate owner"))

        assertFailsWith<DataIntegrityViolationException> {
            service.update(request())
        }
    }

    @Test
    fun `get requires authentication`() {
        SecurityContextHolder.clearContext()

        val exception = assertFailsWith<SettingsException> { service.get() }

        assertEquals(1002, exception.code)
        assertEquals("AUTH-002", exception.responseType)
        assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
        verify(userSettingsRepository, never()).findByOwnerUserId(any())
    }

    @Test
    fun `update rejects an anonymous caller`() {
        SecurityContextHolder.getContext().authentication = AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"),
        )

        val exception = assertFailsWith<SettingsException> { service.update(request()) }

        assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
        verify(userSettingsRepository, never()).save(any())
    }

    @Test
    fun `update rejects a blank principal name`() {
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
            " ",
            "n/a",
            AuthorityUtils.createAuthorityList("ROLE_USER"),
        )

        assertFailsWith<SettingsException> { service.update(request()) }
        verify(userSettingsRepository, never()).save(any())
    }

    private fun request(
        theme: UiTheme = UiTheme.DARK,
        locale: UiLocale = UiLocale.EN,
        sidebarCollapsed: Boolean = false,
        reduceMotion: Boolean = false,
    ) = UserSettingsUpdateRequest().apply {
        this.theme = theme
        this.locale = locale
        this.sidebarCollapsed = sidebarCollapsed
        density = UiDensity.COMFORTABLE
        this.reduceMotion = reduceMotion
    }

    private fun stored(theme: UiTheme) = UserSettingsEntity(
        id = UUID.randomUUID(),
        ownerUserId = "ada",
        theme = theme,
        locale = UiLocale.EN,
        updatedAt = Instant.parse("2026-10-11T08:00:00Z"),
    )

    private fun authenticateAs(userId: String) {
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
            userId,
            "n/a",
            AuthorityUtils.createAuthorityList("ROLE_USER"),
        )
    }
}
