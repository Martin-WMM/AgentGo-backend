package com.agentgo.models.mapper

import com.agentgo.dto.models.ModelCreateRequest
import com.agentgo.dto.models.ModelType
import com.agentgo.dto.models.ModelUpdateRequest
import com.agentgo.models.entity.ModelEntity
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ModelMapperImplTest {
    private val mapper = ModelMapperImpl()

    @Test
    fun mapsCreateRequestToEntity() {
        val request = ModelCreateRequest(
            name = "gpt-4o-mini",
            provider = "openai",
            type = ModelType.LLM,
            configuration = mapOf("model" to "gpt-4o-mini"),
            description = "Chat model",
        )

        val entity = mapper.toEntity("ada", request)

        assertEquals("ada", entity.ownerUserId)
        assertEquals("gpt-4o-mini", entity.name)
        assertEquals("openai", entity.provider)
        assertEquals(ModelType.LLM, entity.type)
        assertEquals(mapOf("model" to "gpt-4o-mini"), entity.configuration)
        assertEquals("Chat model", entity.description)
    }

    @Test
    fun mapsEntityToResponseAndAppliesUpdates() {
        val id = UUID.randomUUID()
        val createdAt = Instant.parse("2026-01-01T00:00:00Z")
        val updatedAt = Instant.parse("2026-01-02T00:00:00Z")
        val entity = ModelEntity(
            id = id,
            ownerUserId = "ada",
            name = "old",
            provider = "openai",
            type = ModelType.LLM,
            configuration = mapOf("model" to "old"),
            description = "old",
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

        mapper.applyUpdate(
            entity,
            ModelUpdateRequest(
                name = "embeddings",
                provider = "ollama",
                type = ModelType.EMBEDDING,
                configuration = mapOf("model" to "nomic"),
                description = null,
            ),
        )
        val response = mapper.toResponse(entity)

        assertEquals("embeddings", entity.name)
        assertEquals(ModelType.EMBEDDING, entity.type)
        assertNull(entity.description)
        assertEquals(id, response.id)
        assertEquals("ollama", response.provider)
        assertEquals(createdAt, response.createdAt)
    }

    @Test
    fun rejectsIncompletePersistedEntities() {
        assertFailsWith<IllegalArgumentException> {
            mapper.toResponse(ModelEntity(name = "incomplete"))
        }
    }
}
