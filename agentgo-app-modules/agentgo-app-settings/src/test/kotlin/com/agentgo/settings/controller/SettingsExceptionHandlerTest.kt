package com.agentgo.settings.controller

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription
import com.agentgo.settings.exception.SettingsException
import com.agentgo.starter.i18n.AgentGoMessages
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.StaticMessageSource
import org.springframework.http.HttpStatus

class SettingsExceptionHandlerTest {
    private val messageSource = StaticMessageSource().apply {
        addMessage("AUTH-002", Locale.ENGLISH, "Authentication is required.")
        addMessage("AUTH-002", Locale.SIMPLIFIED_CHINESE, "需要先登录。")
    }
    private val handler = SettingsExceptionHandler(AgentGoMessages(messageSource))

    @AfterTest
    fun clearLocale() {
        LocaleContextHolder.resetLocaleContext()
    }

    @Test
    fun `maps authentication failure to localized english envelope`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)

        val response = handler.handleSettingsException(authenticationRequired())

        assertEquals(401, response.statusCode.value())
        assertEquals(1002, response.body?.code)
        assertEquals("AUTH-002", response.body?.responseType)
        assertEquals("Authentication is required.", response.body?.message)
        assertFalse(response.body?.success == true)
    }

    @Test
    fun `maps authentication failure to localized chinese envelope`() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE)

        val response = handler.handleSettingsException(authenticationRequired())

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("需要先登录。", response.body?.message)
        assertEquals("AUTH-002", response.body?.responseType)
        assertEquals("AUTHENTICATION", (response.body?.error as? ResponseCodeDescription)?.category)
    }

    private fun authenticationRequired() = SettingsException(
        code = 1002,
        responseType = "AUTH-002",
        status = HttpStatus.UNAUTHORIZED,
        message = "Authentication is required.",
    )
}
