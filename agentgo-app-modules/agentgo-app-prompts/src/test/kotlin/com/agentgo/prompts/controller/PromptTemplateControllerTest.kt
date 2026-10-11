package com.agentgo.prompts.controller

import com.agentgo.commons.dto.http.request.PageRequest
import com.agentgo.commons.dto.http.response.PageResponse
import com.agentgo.commons.dto.http.response.status.success.PromptsResponseCodeRegistry
import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.dto.prompts.PromptTemplateCreateRequest
import com.agentgo.dto.prompts.PromptTemplatePreviewRequest
import com.agentgo.dto.prompts.PromptTemplateRenderRequest
import com.agentgo.dto.prompts.PromptTemplateRenderResponse
import com.agentgo.dto.prompts.PromptTemplateResponse
import com.agentgo.dto.prompts.PromptTemplateUpdateRequest
import com.agentgo.prompts.service.PromptTemplateService
import com.agentgo.starter.i18n.AgentGoMessages
import java.time.Instant
import java.util.Locale
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.StaticMessageSource
import org.springframework.http.HttpStatus

class PromptTemplateControllerTest {
    private val promptTemplateService = mock<PromptTemplateService>()
    private val messageSource = StaticMessageSource().apply {
        addMessage("prompt.created", Locale.ENGLISH, "Prompt template created")
        addMessage("prompt.created", Locale.SIMPLIFIED_CHINESE, "提示词模板已创建")
        addMessage("prompt.retrieved", Locale.ENGLISH, "Prompt template retrieved")
        addMessage("prompt.listed", Locale.ENGLISH, "Prompt templates listed")
        addMessage("prompt.updated", Locale.ENGLISH, "Prompt template updated")
        addMessage("prompt.rendered", Locale.ENGLISH, "Prompt template rendered")
        addMessage("prompt.rendered", Locale.SIMPLIFIED_CHINESE, "提示词模板已渲染")
    }
    private val controller = PromptTemplateController(promptTemplateService, AgentGoMessages(messageSource))

    @AfterTest
    fun clearLocale() {
        LocaleContextHolder.resetLocaleContext()
    }

    @Test
    fun `create wraps service result with created response code and english message`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val request = sampleCreate()
        val response = sampleResponse()
        whenever(promptTemplateService.create(request)).thenReturn(response)

        val created = controller.create(request)

        assertEquals(PromptsResponseCodeRegistry.CREATED, created.code)
        assertEquals(PromptsResponseCodeRegistry.CREATED_TYPE, created.responseType)
        assertEquals("Prompt template created", created.message)
        assertEquals(response, created.data)
        verify(promptTemplateService).create(request)
    }

    @Test
    fun `create returns localized chinese message when accept language is zh-CN`() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE)
        val request = sampleCreate()
        val response = sampleResponse()
        whenever(promptTemplateService.create(request)).thenReturn(response)

        val created = controller.create(request)

        assertEquals("提示词模板已创建", created.message)
        assertEquals(response, created.data)
    }

    @Test
    fun `get wraps service result with retrieved response code`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val response = sampleResponse()
        whenever(promptTemplateService.get(response.id)).thenReturn(response)

        val retrieved = controller.get(response.id)

        assertEquals(PromptsResponseCodeRegistry.RETRIEVED, retrieved.code)
        assertEquals("Prompt template retrieved", retrieved.message)
        assertEquals(response, retrieved.data)
    }

    @Test
    fun `list wraps service result with listed response code`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val response = sampleResponse()
        val request = PageRequest(page = 1, size = 20)
        val page = PageResponse(
            data = listOf(response),
            pageSize = 20,
            totalElements = 1,
            totalPages = 1,
            empty = false,
            first = true,
            last = true,
            size = 1,
            number = 1,
        )
        whenever(promptTemplateService.list(request)).thenReturn(page)

        val listed = controller.list(request)

        assertEquals(PromptsResponseCodeRegistry.LIST_RETRIEVED, listed.code)
        assertEquals("Prompt templates listed", listed.message)
        assertEquals(page, listed.data)
    }

    @Test
    fun `update wraps service result with updated response code`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val response = sampleResponse()
        val request = sampleUpdate()
        whenever(promptTemplateService.update(response.id, request)).thenReturn(response)

        val updated = controller.update(response.id, request)

        assertEquals(PromptsResponseCodeRegistry.UPDATED, updated.code)
        assertEquals("Prompt template updated", updated.message)
        assertEquals(response, updated.data)
    }

    @Test
    fun `delete returns no content after the service removes the template`() {
        val id = UUID.randomUUID()

        val deleted = controller.delete(id)

        assertEquals(HttpStatus.NO_CONTENT, deleted.statusCode)
        assertNull(deleted.body)
        verify(promptTemplateService).delete(id)
    }

    @Test
    fun `preview and render wrap rendered text with the rendered response code`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val id = UUID.randomUUID()
        val rendered = PromptTemplateRenderResponse("Hello Ada")
        val preview = PromptTemplatePreviewRequest().apply {
            engine = PromptEngine.SIMPLE
            content = "Hello {{ name }}"
            variables = mapOf("name" to "Ada")
        }
        val render = PromptTemplateRenderRequest().apply { variables = mapOf("name" to "Ada") }
        whenever(promptTemplateService.preview(preview)).thenReturn(rendered)
        whenever(promptTemplateService.render(id, render)).thenReturn(rendered)

        val previewed = controller.preview(preview)
        val renderedResponse = controller.render(id, render)

        assertEquals(PromptsResponseCodeRegistry.RENDERED, previewed.code)
        assertEquals(PromptsResponseCodeRegistry.RENDERED_TYPE, renderedResponse.responseType)
        assertEquals("Prompt template rendered", previewed.message)
        assertEquals("Hello Ada", renderedResponse.data?.output)
    }

    @Test
    fun `render returns localized chinese message when accept language is zh-CN`() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE)
        val id = UUID.randomUUID()
        val request = PromptTemplateRenderRequest()
        whenever(promptTemplateService.render(id, request)).thenReturn(PromptTemplateRenderResponse("ok"))

        val rendered = controller.render(id, request)

        assertEquals("提示词模板已渲染", rendered.message)
    }

    private fun sampleCreate() = PromptTemplateCreateRequest().apply {
        name = "Default system"
        engine = PromptEngine.SIMPLE
        content = "Hello {{ name }}"
        description = "Greeting"
    }

    private fun sampleUpdate() = PromptTemplateUpdateRequest().apply {
        name = "Default system"
        engine = PromptEngine.SIMPLE
        content = "Hello {{ name }}"
        description = "Greeting"
    }

    private fun sampleResponse() = PromptTemplateResponse(
        id = UUID.randomUUID(),
        name = "Default system",
        engine = PromptEngine.SIMPLE,
        content = "Hello {{ name }}",
        description = "Greeting",
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-01-02T00:00:00Z"),
    )
}
