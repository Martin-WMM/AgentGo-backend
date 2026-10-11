package com.agentgo.dto.models

import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ModelDtoTest {
    @Test
    fun storesCreateRequestDefaults() {
        val request = ModelCreateRequest().apply {
            name = "gpt-4o-mini"
            provider = "openai"
            type = ModelType.LLM
        }

        assertEquals("gpt-4o-mini", request.name)
        assertEquals("openai", request.provider)
        assertEquals(ModelType.LLM, request.type)
        assertEquals(emptyMap(), request.configuration)
        assertNull(request.description)
    }

    @Test
    fun storesUpdateAndResponseFields() {
        val createdAt = Instant.parse("2026-01-01T00:00:00Z")
        val updatedAt = Instant.parse("2026-01-02T00:00:00Z")
        val id = UUID.randomUUID()
        val configuration = mapOf<String, Any?>("model" to "text-embedding-3-small", "dimensions" to 1536)
        val update = ModelUpdateRequest().apply {
            name = "embeddings"
            provider = "openai"
            type = ModelType.EMBEDDING
            this.configuration = configuration
            description = "Workspace embeddings"
        }
        val response = ModelResponse(
            id = id,
            name = update.name,
            provider = update.provider,
            type = checkNotNull(update.type),
            configuration = update.configuration,
            description = update.description,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

        assertEquals(ModelType.EMBEDDING, response.type)
        assertEquals(configuration, response.configuration)
        assertEquals("Workspace embeddings", response.description)
        assertEquals(createdAt, response.createdAt)
        assertEquals(updatedAt, response.updatedAt)
        assertEquals(id, response.id)
    }

    @Test
    fun exposesAllModelTypes() {
        assertEquals(
            listOf(
                ModelType.LLM,
                ModelType.MULTIMODAL,
                ModelType.EMBEDDING,
                ModelType.ASR,
                ModelType.TTS,
                ModelType.SPEECH2SPEECH,
                ModelType.OTHER,
            ),
            ModelType.entries,
        )
        assertEquals(ModelType.OTHER, ModelType.valueOf("OTHER"))
    }

    @Test
    fun requestsMatchAnEqualCopyAndRejectADifferentValue() {
        val configuration = mapOf<String, Any?>("model" to "gpt-4o-mini")
        val create = ModelCreateRequest().apply {
            name = "gpt-4o-mini"
            provider = "openai"
            type = ModelType.LLM
            this.configuration = configuration
            description = "chat"
        }
        val createCopy = ModelCreateRequest().apply {
            name = create.name
            provider = create.provider
            type = create.type
            this.configuration = create.configuration
            description = create.description
        }
        val cleared = ModelCreateRequest().apply {
            name = create.name
            provider = create.provider
            type = null
            this.configuration = create.configuration
            description = null
        }
        val update = ModelUpdateRequest().apply {
            name = "embeddings"
            provider = "openai"
            type = ModelType.EMBEDDING
            this.configuration = configuration
            description = "vectors"
        }
        val updateCopy = ModelUpdateRequest().apply {
            name = update.name
            provider = update.provider
            type = update.type
            this.configuration = update.configuration
            description = update.description
        }

        assertEquals(create, createCopy)
        assertEquals(create.hashCode(), createCopy.hashCode())
        assertEquals(create, create)
        assertNotEquals(create, cleared)
        assertNotEquals(create.hashCode(), cleared.hashCode())
        assertFalse(create.equals("gpt-4o-mini"))
        assertEquals(update, updateCopy)
        assertEquals(update.hashCode(), updateCopy.hashCode())
        assertEquals(update, update)
        assertNotEquals(update, ModelUpdateRequest())
        assertFalse(update.equals(create))
        val response = ModelResponse(
            id = UUID.fromString("22222222-2222-2222-2222-222222222222"),
            name = update.name,
            provider = update.provider,
            type = ModelType.EMBEDDING,
            configuration = configuration,
            description = update.description,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-01-02T00:00:00Z"),
        )
        assertEquals(response, response.copy())
        assertNotEquals(response, response.copy(description = null))
        assertNotEquals(response.hashCode(), response.copy(name = "other").hashCode())
    }
}
