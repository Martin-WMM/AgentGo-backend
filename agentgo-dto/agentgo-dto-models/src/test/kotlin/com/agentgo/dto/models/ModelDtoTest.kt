package com.agentgo.dto.models

import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ModelDtoTest {
    @Test
    fun storesCreateRequestDefaults() {
        val request = ModelCreateRequest(
            name = "gpt-4o-mini",
            provider = "openai",
            type = ModelType.LLM,
        )

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
        val update = ModelUpdateRequest(
            name = "embeddings",
            provider = "openai",
            type = ModelType.EMBEDDING,
            configuration = configuration,
            description = "Workspace embeddings",
        )
        val response = ModelResponse(
            id = id,
            name = update.name,
            provider = update.provider,
            type = update.type,
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
    }
}
