package com.agentgo.prompts.dto

import com.agentgo.dto.presentation.IconSource
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
              "groupName": "chat",
              "name": "Greeting",
              "engine": "SIMPLE",
              "content": "Hello {{ name }}",
              "description": "User greeting",
              "message": "Initial commit",
              "tags": ["system"],
              "metadata": {
                "color": "#1D4ED8",
                "icon": { "source": "ICONIFY", "value": "lucide:bot" }
              }
            }
            """.trimIndent(),
            PromptTemplateCreateRequest::class.java,
        )

        assertEquals("chat", request.groupName)
        assertEquals("Greeting", request.name)
        assertEquals(PromptEngine.SIMPLE, request.engine)
        assertEquals("Hello {{ name }}", request.content)
        assertEquals("User greeting", request.description)
        assertEquals("Initial commit", request.message)
        assertEquals(listOf("system"), request.tags)
        assertEquals("#1D4ED8", request.metadata?.color)
        assertEquals(IconSource.ICONIFY, request.metadata?.icon?.source)
        assertEquals("lucide:bot", request.metadata?.icon?.value)
    }

    @Test
    fun `reads update preview and render requests`() {
        val update = jsonMapper.readValue(
            """{"groupName":"chat","name":"Greeting","engine":"JINJA2","content":"Hello {{ name }}","message":"Revise prompt"}""",
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

        assertEquals("chat", update.groupName)
        assertEquals(PromptEngine.JINJA2, update.engine)
        assertNull(update.description)
        assertEquals("Revise prompt", update.message)
        assertEquals("Ada", preview.variables["name"])
        assertEquals("Ada", render.variables["name"])
    }

    @Test
    fun `reads empty render request as an empty variable map`() {
        val request = jsonMapper.readValue("{}", PromptTemplateRenderRequest::class.java)

        assertEquals(emptyMap(), request.variables)
    }
}
