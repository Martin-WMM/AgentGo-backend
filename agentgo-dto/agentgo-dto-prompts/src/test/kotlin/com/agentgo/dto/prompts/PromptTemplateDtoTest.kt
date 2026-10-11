package com.agentgo.dto.prompts

import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PromptTemplateDtoTest {
    @Test
    fun `create request stores required fields and empty defaults`() {
        val request = PromptTemplateCreateRequest().apply {
            name = "Default system"
            engine = PromptEngine.SIMPLE
            content = "Hello {{ name }}"
        }

        assertEquals("Default system", request.name)
        assertEquals(PromptEngine.SIMPLE, request.engine)
        assertEquals("Hello {{ name }}", request.content)
        assertNull(request.description)
    }

    @Test
    fun `update preview and response preserve supplied fields`() {
        val createdAt = Instant.parse("2026-01-01T00:00:00Z")
        val updatedAt = Instant.parse("2026-01-02T00:00:00Z")
        val id = UUID.randomUUID()
        val variables = mapOf<String, Any?>("name" to "Ada", "count" to 2)
        val update = PromptTemplateUpdateRequest().apply {
            name = "Greeting"
            engine = PromptEngine.JINJA2
            content = "Hello {{ name }}"
            description = "User greeting"
        }
        val preview = PromptTemplatePreviewRequest().apply {
            engine = PromptEngine.SIMPLE
            content = update.content
            this.variables = variables
        }
        val render = PromptTemplateRenderRequest().apply { this.variables = variables }
        val response = PromptTemplateResponse(
            id = id,
            name = update.name,
            engine = checkNotNull(update.engine),
            content = update.content,
            description = update.description,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

        assertEquals(PromptEngine.JINJA2, response.engine)
        assertEquals("User greeting", response.description)
        assertEquals(id, response.id)
        assertEquals(variables, preview.variables)
        assertEquals(variables, render.variables)
        assertEquals("Hello Ada", PromptTemplateRenderResponse("Hello Ada").output)
    }

    @Test
    fun `exposes every engine`() {
        assertEquals(listOf(PromptEngine.SIMPLE, PromptEngine.JINJA2), PromptEngine.entries)
    }
}
