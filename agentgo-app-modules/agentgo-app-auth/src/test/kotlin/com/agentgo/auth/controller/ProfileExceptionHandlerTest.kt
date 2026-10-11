package com.agentgo.auth.controller

import com.agentgo.auth.exception.InvalidProfileRequestException
import com.agentgo.starter.i18n.AgentGoMessages
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.StaticMessageSource

class ProfileExceptionHandlerTest {
    private val messageSource = StaticMessageSource().apply {
        addMessage("profile.phone.invalid", Locale.ENGLISH, "Phone number is invalid.")
        addMessage("profile.phone.invalid", Locale.SIMPLIFIED_CHINESE, "手机号无效。")
    }
    private val handler = ProfileExceptionHandler(AgentGoMessages(messageSource))

    @AfterTest
    fun clearLocale() {
        LocaleContextHolder.resetLocaleContext()
    }

    @Test
    fun `maps invalid profile to localized english envelope`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)

        val response = handler.handleInvalidProfile(
            InvalidProfileRequestException("profile.phone.invalid", "Phone number is invalid."),
        )

        assertEquals(400, response.statusCode.value())
        assertEquals(1006, response.body?.code)
        assertEquals("AUTH-006", response.body?.responseType)
        assertEquals("Phone number is invalid.", response.body?.message)
        assertFalse(response.body?.success == true)
    }

    @Test
    fun `maps invalid profile to localized chinese envelope`() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE)

        val response = handler.handleInvalidProfile(
            InvalidProfileRequestException("profile.phone.invalid", "Phone number is invalid."),
        )

        assertEquals(400, response.statusCode.value())
        assertEquals("手机号无效。", response.body?.message)
    }
}
