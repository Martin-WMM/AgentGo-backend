package com.agentgo.prompts.mapper

import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.dto.prompts.PromptTemplateCreateRequest
import com.agentgo.dto.prompts.PromptTemplateUpdateRequest
import com.agentgo.prompts.entity.PromptTemplateEntity
import java.time.Instant
import java.util.UUID
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
                name = "Greeting"
                engine = PromptEngine.SIMPLE
                content = "Hello {{ name }}"
                description = "Hi"
            },
        )

        assertEquals("ada", entity.ownerUserId)
        assertEquals("Greeting", entity.name)
        assertEquals(PromptEngine.SIMPLE, entity.engine)
        assertEquals("Hello {{ name }}", entity.content)
        assertEquals("Hi", entity.description)
    }

    @Test
    fun `applyUpdate replaces mutable fields and toResponse requires generated values`() {
        val entity = PromptTemplateEntity(ownerUserId = "ada", name = "Old", content = "old")
        mapper.applyUpdate(
            entity,
            PromptTemplateUpdateRequest().apply {
                name = "New"
                engine = PromptEngine.JINJA2
                content = "Hello {{ name }}"
                description = null
            },
        )
        entity.id = UUID.randomUUID()
        entity.createdAt = Instant.parse("2026-01-01T00:00:00Z")
        entity.updatedAt = Instant.parse("2026-01-02T00:00:00Z")

        val response = mapper.toResponse(entity)

        assertEquals("New", response.name)
        assertEquals(PromptEngine.JINJA2, response.engine)
        assertEquals(entity.id, response.id)
    }

    @Test
    fun `toResponse rejects a row that has not been persisted`() {
        val entity = PromptTemplateEntity(ownerUserId = "ada", name = "Draft", content = "Hello")

        assertFailsWith<IllegalArgumentException> { mapper.toResponse(entity) }

        entity.id = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException> { mapper.toResponse(entity) }

        entity.createdAt = Instant.parse("2026-01-01T00:00:00Z")
        assertFailsWith<IllegalArgumentException> { mapper.toResponse(entity) }
    }
}
