package com.agentgo.dto.prompts

import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
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

    @Test
    fun `requests match an equal copy and reject a different value`() {
        val variables = mapOf<String, Any?>("name" to "Ada")
        val create = PromptTemplateCreateRequest().apply {
            name = "Greeting"
            engine = PromptEngine.SIMPLE
            content = "Hello"
            description = "hi"
        }
        val createCopy = PromptTemplateCreateRequest().apply {
            name = create.name
            engine = create.engine
            content = create.content
            description = create.description
        }
        val update = PromptTemplateUpdateRequest().apply {
            name = create.name
            engine = PromptEngine.JINJA2
            content = create.content
            description = create.description
        }
        val updateCopy = PromptTemplateUpdateRequest().apply {
            name = update.name
            engine = update.engine
            content = update.content
            description = update.description
        }
        val preview = PromptTemplatePreviewRequest().apply {
            engine = PromptEngine.SIMPLE
            content = "Hello"
            this.variables = variables
        }
        val previewCopy = PromptTemplatePreviewRequest().apply {
            engine = preview.engine
            content = preview.content
            this.variables = preview.variables
        }
        val render = PromptTemplateRenderRequest().apply { this.variables = variables }
        val renderCopy = PromptTemplateRenderRequest().apply { this.variables = render.variables }

        assertEquals(create, createCopy)
        assertEquals(create.hashCode(), createCopy.hashCode())
        assertEquals(create, create)
        assertNotEquals(create, PromptTemplateCreateRequest())
        assertFalse(create.equals("Greeting"))
        assertEquals(update, updateCopy)
        assertEquals(update.hashCode(), updateCopy.hashCode())
        assertNotEquals(update, PromptTemplateUpdateRequest())
        assertFalse(update.equals(create))
        assertEquals(preview, previewCopy)
        assertEquals(preview.hashCode(), previewCopy.hashCode())
        assertNotEquals(preview, PromptTemplatePreviewRequest())
        assertFalse(preview.equals(variables))
        assertEquals(render, renderCopy)
        assertEquals(render.hashCode(), renderCopy.hashCode())
        assertNotEquals(render, PromptTemplateRenderRequest())
        assertFalse(render.equals(variables))
    }
}
