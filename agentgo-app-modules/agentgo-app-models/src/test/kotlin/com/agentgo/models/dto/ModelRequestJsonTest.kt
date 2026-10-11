package com.agentgo.models.dto

import com.agentgo.dto.models.ModelCreateRequest
import com.agentgo.dto.models.ModelType
import com.agentgo.dto.models.ModelUpdateRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import tools.jackson.databind.json.JsonMapper

/**
 * Proves Spring Boot 4's Jackson 3 mapper can read model request bodies.
 *
 * Kotlin data classes are not deserialized by that mapper. These beans must stay mutable.
 */
class ModelRequestJsonTest {
    private val jsonMapper = JsonMapper.builder().build()

    @Test
    fun readsCreateRequestFromJson() {
        val request = jsonMapper.readValue(
            """
            {
              "name": "gpt-4o-mini",
              "provider": "openai",
              "type": "LLM",
              "configuration": {"baseUrl": "https://api.openai.com/v1", "model": "gpt-4o-mini"},
              "description": "Chat model"
            }
            """.trimIndent(),
            ModelCreateRequest::class.java,
        )

        assertEquals("gpt-4o-mini", request.name)
        assertEquals("openai", request.provider)
        assertEquals(ModelType.LLM, request.type)
        assertEquals("https://api.openai.com/v1", request.configuration["baseUrl"])
        assertEquals("gpt-4o-mini", request.configuration["model"])
        assertEquals("Chat model", request.description)
    }

    @Test
    fun readsUpdateRequestDefaultsWhenFieldsAreOmitted() {
        val request = jsonMapper.readValue("{}", ModelUpdateRequest::class.java)

        assertEquals("", request.name)
        assertEquals("", request.provider)
        assertNull(request.type)
        assertEquals(emptyMap(), request.configuration)
        assertNull(request.description)
    }
}
