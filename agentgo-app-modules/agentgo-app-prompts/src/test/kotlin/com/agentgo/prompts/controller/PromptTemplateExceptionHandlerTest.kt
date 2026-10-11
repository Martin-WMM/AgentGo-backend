package com.agentgo.prompts.controller

import com.agentgo.prompts.exception.DuplicatePromptTemplateNameException
import com.agentgo.prompts.exception.PromptTemplateNotFoundException
import com.agentgo.prompts.exception.PromptTemplateRenderException
import com.agentgo.prompts.exception.UnauthorizedPromptTemplateException
import com.agentgo.starter.i18n.AgentGoMessages
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.StaticMessageSource
import org.springframework.http.HttpStatus

class PromptTemplateExceptionHandlerTest {
    private val messageSource = StaticMessageSource().apply {
        addMessage("PROMPT-001", Locale.ENGLISH, "The requested prompt template was not found.")
        addMessage("PROMPT-001", Locale.SIMPLIFIED_CHINESE, "未找到请求的提示词模板。")
        addMessage("PROMPT-003", Locale.ENGLISH, "A prompt template with this name already exists for this user.")
        addMessage("PROMPT-004", Locale.ENGLISH, "The prompt template could not be rendered.")
        addMessage("PROMPT-005", Locale.ENGLISH, "You do not have access to this prompt template.")
    }
    private val handler = PromptTemplateExceptionHandler(AgentGoMessages(messageSource))

    @AfterTest
    fun clearLocale() {
        LocaleContextHolder.resetLocaleContext()
    }

    @Test
    fun `maps not found exception to localized english envelope`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)

        val response = handler.handlePromptTemplateException(PromptTemplateNotFoundException())

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals(9001, response.body?.code)
        assertEquals("PROMPT-001", response.body?.responseType)
        assertEquals("The requested prompt template was not found.", response.body?.message)
        assertFalse(response.body?.success == true)
    }

    @Test
    fun `maps not found exception to localized chinese envelope`() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE)

        val response = handler.handlePromptTemplateException(PromptTemplateNotFoundException())

        assertEquals("未找到请求的提示词模板。", response.body?.message)
    }

    @Test
    fun `maps duplicate render and unauthorized failures to their status codes`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)

        val duplicate = handler.handlePromptTemplateException(DuplicatePromptTemplateNameException())
        val render = handler.handlePromptTemplateException(PromptTemplateRenderException())
        val unauthorized = handler.handlePromptTemplateException(UnauthorizedPromptTemplateException())

        assertEquals(HttpStatus.CONFLICT, duplicate.statusCode)
        assertEquals(9003, duplicate.body?.code)
        assertEquals(HttpStatus.BAD_REQUEST, render.statusCode)
        assertEquals("PROMPT-004", render.body?.responseType)
        assertEquals(HttpStatus.FORBIDDEN, unauthorized.statusCode)
        assertEquals("PROMPT-005", unauthorized.body?.responseType)
        assertEquals("You do not have access to this prompt template.", unauthorized.body?.message)
    }
}
