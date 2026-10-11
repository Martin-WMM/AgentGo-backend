package com.agentgo.settings.mapper

import com.agentgo.dto.settings.UiDensity
import com.agentgo.dto.settings.UiLocale
import com.agentgo.dto.settings.UiTheme
import com.agentgo.dto.settings.UserSettingsUpdateRequest
import com.agentgo.settings.entity.UserSettingsEntity
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserSettingsMapperImplTest {
    private val mapper = UserSettingsMapperImpl()

    @Test
    fun `unpersisted defaults follow the system theme and english`() {
        val defaults = mapper.unpersistedDefaults()

        assertEquals(UiTheme.SYSTEM, defaults.theme)
        assertEquals(UiLocale.EN, defaults.locale)
        assertFalse(defaults.sidebarCollapsed)
        assertEquals(UiDensity.COMFORTABLE, defaults.density)
        assertFalse(defaults.reduceMotion)
        assertFalse(defaults.persisted)
        assertNull(defaults.updatedAt)
    }

    @Test
    fun `to response requires an update timestamp`() {
        val entity = UserSettingsEntity(ownerUserId = "ada")

        assertFailsWith<IllegalArgumentException> { mapper.toResponse(entity) }
    }

    @Test
    fun `to response marks a stored row as persisted`() {
        val updatedAt = Instant.parse("2026-10-11T08:00:00Z")
        val entity = UserSettingsEntity(
            id = UUID.randomUUID(),
            ownerUserId = "ada",
            theme = UiTheme.DARK,
            locale = UiLocale.ZH_CN,
            sidebarCollapsed = true,
            density = UiDensity.COMPACT,
            reduceMotion = true,
            updatedAt = updatedAt,
        )

        val response = mapper.toResponse(entity)

        assertEquals(UiTheme.DARK, response.theme)
        assertEquals(UiLocale.ZH_CN, response.locale)
        assertTrue(response.sidebarCollapsed)
        assertEquals(UiDensity.COMPACT, response.density)
        assertTrue(response.reduceMotion)
        assertTrue(response.persisted)
        assertEquals(updatedAt, response.updatedAt)
    }

    @Test
    fun `new entity and apply update copy the request`() {
        val request = UserSettingsUpdateRequest().apply {
            theme = UiTheme.LIGHT
            locale = UiLocale.EN
            sidebarCollapsed = true
            density = UiDensity.COMPACT
            reduceMotion = false
        }
        val created = mapper.newEntity("ada", request)
        assertEquals("ada", created.ownerUserId)
        assertEquals(UiTheme.LIGHT, created.theme)
        assertTrue(created.sidebarCollapsed)

        val existing = UserSettingsEntity(ownerUserId = "ada", theme = UiTheme.SYSTEM)
        val update = UserSettingsUpdateRequest().apply {
            theme = UiTheme.DARK
            locale = UiLocale.EN
            sidebarCollapsed = true
            density = UiDensity.COMPACT
            reduceMotion = true
        }
        mapper.applyUpdate(existing, update)
        assertEquals(UiTheme.DARK, existing.theme)
        assertEquals(UiLocale.EN, existing.locale)
        assertTrue(existing.reduceMotion)
        assertEquals("ada", existing.ownerUserId)
    }
}
