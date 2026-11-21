package com.agentgo.events.controller

import com.agentgo.events.exception.InvalidSystemEventRequestException
import com.agentgo.events.exception.SystemEventException
import com.agentgo.events.exception.SystemEventNotFoundException
import com.agentgo.starter.i18n.AgentGoMessages
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.StaticMessageSource
import org.springframework.http.HttpStatus

class SystemEventExceptionHandlerTest {
    private val messageSource = StaticMessageSource().apply {
        addMessage("EVENT-001", Locale.ENGLISH, "The requested system event was not found.")
        addMessage("EVENT-001", Locale.SIMPLIFIED_CHINESE, "未找到请求的系统事件。")
        addMessage("EVENT-002", Locale.ENGLISH, "The system event request is invalid.")
        addMessage("AUTH-002", Locale.ENGLISH, "Authentication is required.")
    }
    private val handler = SystemEventExceptionHandler(AgentGoMessages(messageSource))

    @AfterTest
    fun clearLocale() {
        LocaleContextHolder.resetLocaleContext()
    }

    @Test
    fun `maps not found to a localized english envelope`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)

        val response = handler.handleSystemEventException(SystemEventNotFoundException())

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals(9701, response.body?.code)
        assertEquals("EVENT-001", response.body?.responseType)
        assertEquals("The requested system event was not found.", response.body?.message)
        assertFalse(response.body?.success == true)
    }

    @Test
    fun `maps not found to a localized chinese envelope`() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE)

        val response = handler.handleSystemEventException(SystemEventNotFoundException())

        assertEquals("未找到请求的系统事件。", response.body?.message)
    }

    @Test
    fun `maps an invalid request and missing authentication to their statuses`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)

        val invalid = handler.handleSystemEventException(
            InvalidSystemEventRequestException("The event time is older than the three-month retention window."),
        )
        val unauthenticated = handler.handleSystemEventException(
            SystemEventException(
                code = 1002,
                responseType = "AUTH-002",
                status = HttpStatus.UNAUTHORIZED,
                message = "Authentication is required.",
            ),
        )

        assertEquals(HttpStatus.BAD_REQUEST, invalid.statusCode)
        assertEquals(9702, invalid.body?.code)
        assertEquals("EVENT-002", invalid.body?.responseType)
        assertEquals("The system event request is invalid.", invalid.body?.message)
        assertEquals(HttpStatus.UNAUTHORIZED, unauthenticated.statusCode)
        assertEquals("AUTH-002", unauthenticated.body?.responseType)
        assertEquals("Authentication is required.", unauthenticated.body?.message)
    }
}
