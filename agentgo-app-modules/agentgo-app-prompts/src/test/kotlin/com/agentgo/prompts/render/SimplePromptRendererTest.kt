package com.agentgo.prompts.render

import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.prompts.exception.InvalidPromptTemplateRequestException
import com.agentgo.prompts.exception.PromptTemplateRenderException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SimplePromptRendererTest {
    private val renderer = SimplePromptRenderer()

    @Test
    fun `replaces identifiers and preserves surrounding text`() {
        val output = renderer.render("Hello {{ name }}, count {{ count }}.", mapOf("name" to "Ada", "count" to 2))

        assertEquals("Hello Ada, count 2.", output)
        assertEquals(PromptEngine.SIMPLE, renderer.engine)
    }

    @Test
    fun `allows whitespace inside placeholders and boolean values`() {
        assertEquals("true", renderer.render("{{  ready  }}", mapOf("ready" to true)))
    }

    @Test
    fun `validate accepts a template that does not yet have variables`() {
        renderer.validate("Hello {{ name }}")
    }

    @Test
    fun `rejects unclosed expressions unexpected closings and non identifiers`() {
        assertFailsWith<InvalidPromptTemplateRequestException> { renderer.validate("Hello {{ name") }
        assertFailsWith<InvalidPromptTemplateRequestException> { renderer.validate("Hello }}") }
        assertFailsWith<InvalidPromptTemplateRequestException> { renderer.validate("Hello {{ name|upper }}") }
        assertFailsWith<InvalidPromptTemplateRequestException> { renderer.validate("Hello {{ user.name }}") }
    }

    @Test
    fun `render fails when a placeholder variable is missing`() {
        assertFailsWith<PromptTemplateRenderException> {
            renderer.render("Hello {{ name }}", emptyMap())
        }
    }

    @Test
    fun `render rejects null collections and non finite numbers`() {
        assertFailsWith<InvalidPromptTemplateRequestException> {
            renderer.render("Hello {{ name }}", mapOf("name" to null))
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            renderer.render("Hello {{ name }}", mapOf("name" to listOf("Ada")))
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            renderer.render("Hello {{ name }}", mapOf("name" to Double.NaN))
        }
    }

    @Test
    fun `render rejects output that exceeds the configured limit`() {
        val content = "x".repeat(PromptTemplateLimits.OUTPUT_MAX_LENGTH + 1)

        assertFailsWith<PromptTemplateRenderException> { renderer.render(content, emptyMap()) }
    }
}
