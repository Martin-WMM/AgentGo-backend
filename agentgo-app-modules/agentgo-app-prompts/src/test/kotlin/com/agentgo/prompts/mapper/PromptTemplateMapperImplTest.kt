package com.agentgo.prompts.mapper

import com.agentgo.dto.presentation.IconSource
import com.agentgo.dto.presentation.ResourceIcon
import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.dto.prompts.PromptTemplateCreateRequest
import com.agentgo.dto.prompts.PromptTemplateMetadata
import com.agentgo.dto.prompts.PromptTemplateUpdateRequest
import com.agentgo.dto.prompts.PromptTemplateVersions
import com.agentgo.dto.workspace.WorkspaceColors
import com.agentgo.prompts.entity.PromptTemplateEntity
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PromptTemplateMapperImplTest {
    private val mapper = PromptTemplateMapperImpl()

    @Test
    fun `toEntity copies the owner and request fields`() {
        val entity = mapper.toEntity(
            "ada",
            PromptTemplateCreateRequest().apply {
                groupName = "  chat  "
                name = "  Greeting  "
                engine = PromptEngine.SIMPLE
                content = " Hello {{ name }} "
                description = "  "
                tags = listOf(" draft ", "draft", " ")
            },
        )

        assertEquals("ada", entity.ownerUserId)
        assertEquals("chat", entity.groupName)
        assertEquals("Greeting", entity.name)
        assertEquals(PromptTemplateVersions.LATEST, entity.version)
        assertEquals(1, entity.headRevision)
        assertEquals(PromptEngine.SIMPLE, entity.engine)
        assertEquals("Hello {{ name }}", entity.content)
        assertEquals(null, entity.description)
        assertEquals(listOf("draft"), entity.tags)
        assertEquals(WorkspaceColors.DEFAULT, entity.metadata.color)
    }

    @Test
    fun `applyUpdate replaces mutable fields and toResponse requires generated values`() {
        val entity = PromptTemplateEntity(ownerUserId = "ada", name = "Old", content = "old")
        mapper.applyUpdate(
            entity,
            PromptTemplateUpdateRequest().apply {
                groupName = "chat"
                name = "New"
                engine = PromptEngine.JINJA2
                content = "Hello {{ name }}"
                description = null
                metadata = PromptTemplateMetadata().apply {
                    color = "#1D4ED8"
                    icon = ResourceIcon().apply {
                        source = IconSource.ICONIFY
                        value = "lucide:bot"
                    }
                }
            },
        )
        entity.id = 1L
        entity.createdAt = Instant.parse("2026-01-01T00:00:00Z")
        entity.updatedAt = Instant.parse("2026-01-02T00:00:00Z")

        val response = mapper.toResponse(entity)

        assertEquals("chat", response.groupName)
        assertEquals("New", response.name)
        assertEquals(PromptTemplateVersions.LATEST, response.version)
        assertEquals(1, response.revision)
        assertEquals(PromptEngine.JINJA2, response.engine)
        assertEquals(IconSource.ICONIFY, response.metadata.icon?.source)
        assertEquals(entity.id, response.id)
    }

    @Test
    fun `toResponse rejects a row that has not been persisted`() {
        val entity = PromptTemplateEntity(ownerUserId = "ada", name = "Draft", content = "Hello")

        assertFailsWith<IllegalArgumentException> { mapper.toResponse(entity) }

        entity.id = 2L
        assertFailsWith<IllegalArgumentException> { mapper.toResponse(entity) }

        entity.createdAt = Instant.parse("2026-01-01T00:00:00Z")
        assertFailsWith<IllegalArgumentException> { mapper.toResponse(entity) }
    }
}
