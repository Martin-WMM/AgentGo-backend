package com.agentgo.dto.prompts

import com.agentgo.dto.presentation.IconSource
import com.agentgo.dto.presentation.ResourceIcon
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
        val id = 1L
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
            groupName = "chat",
            name = update.name,
            version = PromptTemplateVersions.LATEST,
            revision = 1,
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
            groupName = "chat"
            name = "Greeting"
            engine = PromptEngine.SIMPLE
            content = "Hello"
            description = "hi"
            message = "Initial commit"
            tags = listOf("system")
        }
        val createCopy = PromptTemplateCreateRequest().apply {
            groupName = create.groupName
            name = create.name
            engine = create.engine
            content = create.content
            description = create.description
            message = create.message
            tags = create.tags
            metadata = create.metadata
        }
        val update = PromptTemplateUpdateRequest().apply {
            groupName = create.groupName
            name = create.name
            engine = PromptEngine.JINJA2
            content = create.content
            description = create.description
            message = "Revise prompt"
            tags = create.tags
        }
        val updateCopy = PromptTemplateUpdateRequest().apply {
            groupName = update.groupName
            name = update.name
            engine = update.engine
            content = update.content
            description = update.description
            message = update.message
            tags = update.tags
            metadata = update.metadata
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
        assertEquals(PromptTemplateCreateRequest().hashCode(), PromptTemplateCreateRequest().hashCode())
        assertEquals(PromptTemplateUpdateRequest().hashCode(), PromptTemplateUpdateRequest().hashCode())
        assertEquals(PromptTemplatePreviewRequest().hashCode(), PromptTemplatePreviewRequest().hashCode())
    }

    @Test
    fun `metadata duplicates color and icon and compares by value`() {
        val icon = ResourceIcon().apply {
            source = IconSource.ICONIFY
            value = "lucide:bot"
        }
        val metadata = PromptTemplateMetadata().apply {
            color = "#1D4ED8"
            this.icon = icon
        }
        val duplicated = metadata.duplicate()

        assertEquals("#1D4ED8", duplicated.color)
        assertEquals(icon, duplicated.icon)
        assertFalse(duplicated.icon === icon)
        assertEquals(metadata, duplicated)
        assertEquals(metadata.hashCode(), duplicated.hashCode())
        assertEquals(metadata, metadata)
        assertNotEquals(metadata, PromptTemplateMetadata())
        assertNotEquals(metadata, metadata.duplicate().apply { color = "#B91C1C" })
        assertNotEquals(metadata, metadata.duplicate().apply { this.icon = null })
        assertFalse(metadata.equals("#1D4ED8"))
        assertTrue(metadata.toString().contains("#1D4ED8"))

        val bare = PromptTemplateMetadata()
        val bareCopy = bare.duplicate()
        assertNull(bareCopy.color)
        assertNull(bareCopy.icon)
        assertEquals(bare, bareCopy)
        assertEquals(bare.hashCode(), bareCopy.hashCode())
        assertEquals("PromptTemplateMetadata(color=null, icon=null)", bare.toString())
    }

    @Test
    fun `revision response keeps commit fields through copy and equality`() {
        val createdAt = Instant.parse("2026-03-01T00:00:00Z")
        val metadata = PromptTemplateMetadata().apply {
            color = "#0F766E"
            icon = ResourceIcon().apply {
                source = IconSource.URL
                value = "https://cdn.example.com/bot.png"
            }
        }
        val revision = PromptTemplateRevisionResponse(
            id = 2L,
            templateId = 3L,
            version = PromptTemplateVersions.label(1),
            revision = 1,
            message = "Initial commit",
            author = "ada",
            diff = "+content",
            groupName = "chat",
            name = "Greeting",
            engine = PromptEngine.SIMPLE,
            content = "Hello",
            description = "hi",
            tags = listOf("system"),
            metadata = metadata,
            createdAt = createdAt,
        )
        val copied = revision.copy()
        val next = revision.copy(
            version = PromptTemplateVersions.label(2),
            revision = 2,
            parentRevision = 1,
            message = "Revise prompt",
        )

        assertEquals("latest", PromptTemplateVersions.LATEST)
        assertEquals("1", revision.version)
        assertNull(revision.parentRevision)
        assertEquals(revision, copied)
        assertEquals(revision.hashCode(), copied.hashCode())
        assertEquals(revision, revision)
        assertNotEquals(revision, next)
        assertNotEquals(revision.hashCode(), next.hashCode())
        assertEquals(1, next.parentRevision)
        assertEquals("2", next.version)
        assertEquals("Revise prompt", next.message)
        assertFalse(revision.equals("Initial commit"))
        assertTrue(revision.toString().contains("Initial commit"))
        assertTrue(revision.toString().contains("Greeting"))
    }

    @Test
    fun `response and render result support copy equality and text`() {
        val createdAt = Instant.parse("2026-01-01T00:00:00Z")
        val updatedAt = Instant.parse("2026-01-02T00:00:00Z")
        val response = PromptTemplateResponse(
            id = 4L,
            groupName = "chat",
            name = "Greeting",
            version = PromptTemplateVersions.LATEST,
            revision = 3,
            engine = PromptEngine.JINJA2,
            content = "Hello {{ name }}",
            description = "User greeting",
            tags = listOf("chat"),
            metadata = PromptTemplateMetadata().apply { color = "#15803D" },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
        val rendered = PromptTemplateRenderResponse("Hello Ada")

        assertEquals(response, response.copy())
        assertEquals(response.hashCode(), response.copy().hashCode())
        assertNotEquals(response, response.copy(name = "Other", tags = emptyList(), description = null))
        assertFalse(response.equals(rendered))
        assertTrue(response.toString().contains("Greeting"))
        assertEquals(rendered, rendered.copy())
        assertEquals(rendered.hashCode(), rendered.copy(output = "Hello Ada").hashCode())
        assertNotEquals(rendered, rendered.copy(output = "Bye"))
        assertTrue(rendered.toString().contains("Hello Ada"))
    }
}
