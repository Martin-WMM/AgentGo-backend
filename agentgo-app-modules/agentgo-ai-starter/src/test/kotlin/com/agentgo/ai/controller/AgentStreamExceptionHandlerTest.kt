package com.agentgo.ai.controller

import com.agentgo.ai.exception.AgentStreamBusyException
import com.agentgo.commons.dto.http.response.status.error.AgentResponseCodeRegistry
import com.agentgo.starter.i18n.AgentGoMessages
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.springframework.context.support.StaticMessageSource
import org.springframework.http.HttpStatus

class AgentStreamExceptionHandlerTest {
    private val messageSource = StaticMessageSource().apply {
        addMessage(AgentResponseCodeRegistry.BUSY_TYPE, Locale.ENGLISH, "Agent stream workers are busy. Retry later.")
        addMessage(AgentResponseCodeRegistry.BUSY_TYPE, Locale.SIMPLIFIED_CHINESE, "智能体流式执行池已满，请稍后重试。")
    }
    private val handler = AgentStreamExceptionHandler(AgentGoMessages(messageSource))

    @AfterTest
    fun clearLocale() {
        org.springframework.context.i18n.LocaleContextHolder.resetLocaleContext()
    }

    @Test
    fun `handler maps a busy pool to the agent response code`() {
        org.springframework.context.i18n.LocaleContextHolder.setLocale(Locale.ENGLISH)

        val response = handler.handleAgentStreamException(AgentStreamBusyException())

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.statusCode)
        assertEquals(AgentResponseCodeRegistry.BUSY, response.body?.code)
        assertEquals(AgentResponseCodeRegistry.BUSY_TYPE, response.body?.responseType)
        assertEquals("Agent stream workers are busy. Retry later.", response.body?.message)
        assertFalse(response.body?.success ?: true)
    }
}
