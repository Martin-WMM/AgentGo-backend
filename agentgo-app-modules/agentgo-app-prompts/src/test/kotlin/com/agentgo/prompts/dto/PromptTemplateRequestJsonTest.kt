package com.agentgo.prompts.dto

import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.dto.prompts.PromptTemplateCreateRequest
import com.agentgo.dto.prompts.PromptTemplatePreviewRequest
import com.agentgo.dto.prompts.PromptTemplateRenderRequest
import com.agentgo.dto.prompts.PromptTemplateUpdateRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import tools.jackson.databind.json.JsonMapper

/**
 * Proves Spring Boot 4's Jackson 3 mapper can read prompt-template request bodies.
 *
 * Kotlin data classes are not deserialized by that mapper. These beans must stay mutable.
 */
class PromptTemplateRequestJsonTest {
    private val jsonMapper = JsonMapper.builder().build()

    @Test
    fun `reads create request from json`() {
        val request = jsonMapper.readValue(
            """
            {
              "name": "Greeting",
              "engine": "SIMPLE",
              "content": "Hello {{ name }}",
              "description": "User greeting"
            }
            """.trimIndent(),
            PromptTemplateCreateRequest::class.java,
        )

        assertEquals("Greeting", request.name)
        assertEquals(PromptEngine.SIMPLE, request.engine)
        assertEquals("Hello {{ name }}", request.content)
        assertEquals("User greeting", request.description)
    }

    @Test
    fun `reads update preview and render requests`() {
        val update = jsonMapper.readValue(
            """{"name":"Greeting","engine":"JINJA2","content":"Hello {{ name }}"}""",
            PromptTemplateUpdateRequest::class.java,
        )
        val preview = jsonMapper.readValue(
            """{"engine":"SIMPLE","content":"Hello {{ name }}","variables":{"name":"Ada"}}""",
            PromptTemplatePreviewRequest::class.java,
        )
        val render = jsonMapper.readValue(
            """{"variables":{"name":"Ada"}}""",
            PromptTemplateRenderRequest::class.java,
        )

        assertEquals(PromptEngine.JINJA2, update.engine)
        assertNull(update.description)
        assertEquals("Ada", preview.variables["name"])
        assertEquals("Ada", render.variables["name"])
    }

    @Test
    fun `reads empty render request as an empty variable map`() {
        val request = jsonMapper.readValue("{}", PromptTemplateRenderRequest::class.java)

        assertEquals(emptyMap(), request.variables)
    }
}
