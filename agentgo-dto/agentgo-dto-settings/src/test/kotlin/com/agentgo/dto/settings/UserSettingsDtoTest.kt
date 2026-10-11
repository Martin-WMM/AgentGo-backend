package com.agentgo.dto.settings

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserSettingsDtoTest {
    @Test
    fun `update request stores the client preferences`() {
        val request = UserSettingsUpdateRequest().apply {
            theme = UiTheme.DARK
            locale = UiLocale.ZH_CN
            sidebarCollapsed = true
            density = UiDensity.COMPACT
            reduceMotion = true
        }

        assertEquals(UiTheme.DARK, request.theme)
        assertEquals(UiLocale.ZH_CN, request.locale)
        assertTrue(request.sidebarCollapsed)
        assertEquals(UiDensity.COMPACT, request.density)
        assertTrue(request.reduceMotion)
    }

    @Test
    fun `response keeps persistence metadata`() {
        val updatedAt = Instant.parse("2026-10-11T08:00:00Z")
        val response = UserSettingsResponse(
            theme = UiTheme.SYSTEM,
            locale = UiLocale.EN,
            sidebarCollapsed = false,
            density = UiDensity.COMFORTABLE,
            reduceMotion = false,
            persisted = false,
        )

        assertEquals(UiTheme.SYSTEM, response.theme)
        assertEquals(UiLocale.EN, response.locale)
        assertFalse(response.persisted)
        assertNull(response.updatedAt)
        assertEquals(updatedAt, response.copy(persisted = true, updatedAt = updatedAt).updatedAt)
    }
}
