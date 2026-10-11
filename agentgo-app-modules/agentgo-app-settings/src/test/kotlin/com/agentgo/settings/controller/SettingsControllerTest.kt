package com.agentgo.settings.controller

import com.agentgo.commons.dto.http.response.status.success.SettingsResponseCodeRegistry
import com.agentgo.dto.settings.UiDensity
import com.agentgo.dto.settings.UiLocale
import com.agentgo.dto.settings.UiTheme
import com.agentgo.dto.settings.UserSettingsResponse
import com.agentgo.dto.settings.UserSettingsUpdateRequest
import com.agentgo.settings.service.UserSettingsService
import com.agentgo.starter.i18n.AgentGoMessages
import java.time.Instant
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.StaticMessageSource

class SettingsControllerTest {
    private val userSettingsService = mock<UserSettingsService>()
    private val messageSource = StaticMessageSource().apply {
        addMessage("settings.retrieved", Locale.ENGLISH, "System settings retrieved")
        addMessage("settings.retrieved", Locale.SIMPLIFIED_CHINESE, "系统设置已获取")
        addMessage("settings.updated", Locale.ENGLISH, "System settings updated")
        addMessage("settings.updated", Locale.SIMPLIFIED_CHINESE, "系统设置已更新")
    }
    private val controller = SettingsController(userSettingsService, AgentGoMessages(messageSource))

    @AfterTest
    fun clearLocale() {
        LocaleContextHolder.resetLocaleContext()
    }

    @Test
    fun `get wraps service result with retrieved response code and english message`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val response = sampleResponse(persisted = false)
        whenever(userSettingsService.get()).thenReturn(response)

        val http = controller.get()

        assertEquals(SettingsResponseCodeRegistry.RETRIEVED, http.code)
        assertEquals(SettingsResponseCodeRegistry.RETRIEVED_TYPE, http.responseType)
        assertEquals("System settings retrieved", http.message)
        assertEquals(response, http.data)
    }

    @Test
    fun `get selects the chinese message`() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE)
        whenever(userSettingsService.get()).thenReturn(sampleResponse(persisted = true))

        assertEquals("系统设置已获取", controller.get().message)
    }

    @Test
    fun `update wraps service result with updated response code and english message`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val request = sampleRequest()
        val response = sampleResponse(persisted = true)
        whenever(userSettingsService.update(request)).thenReturn(response)

        val http = controller.update(request)

        assertEquals(SettingsResponseCodeRegistry.UPDATED, http.code)
        assertEquals(SettingsResponseCodeRegistry.UPDATED_TYPE, http.responseType)
        assertEquals("System settings updated", http.message)
        assertEquals(response, http.data)
        verify(userSettingsService).update(request)
    }

    @Test
    fun `update selects the chinese message`() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE)
        val request = sampleRequest()
        whenever(userSettingsService.update(request)).thenReturn(sampleResponse(persisted = true))

        assertEquals("系统设置已更新", controller.update(request).message)
    }

    private fun sampleRequest() = UserSettingsUpdateRequest().apply {
        theme = UiTheme.DARK
        locale = UiLocale.ZH_CN
        sidebarCollapsed = true
        density = UiDensity.COMPACT
        reduceMotion = true
    }

    private fun sampleResponse(persisted: Boolean) = UserSettingsResponse(
        theme = UiTheme.DARK,
        locale = UiLocale.ZH_CN,
        sidebarCollapsed = true,
        density = UiDensity.COMPACT,
        reduceMotion = true,
        persisted = persisted,
        updatedAt = if (persisted) Instant.parse("2026-10-11T08:00:00Z") else null,
    )
}
