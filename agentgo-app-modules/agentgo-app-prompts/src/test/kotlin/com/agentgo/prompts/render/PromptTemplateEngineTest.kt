package com.agentgo.prompts.render

import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.prompts.exception.InvalidPromptTemplateRequestException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PromptTemplateEngineTest {
    private val engine = PromptTemplateEngine(listOf(SimplePromptRenderer(), Jinja2PromptRenderer()))

    @Test
    fun `validates and renders with the selected engine`() {
        engine.validate(PromptEngine.SIMPLE, "Hello {{ name }}")

        assertEquals("Hello Ada", engine.render(PromptEngine.SIMPLE, "Hello {{ name }}", mapOf("name" to "Ada")))
    }

    @Test
    fun `rejects invalid variable names before rendering`() {
        assertFailsWith<InvalidPromptTemplateRequestException> {
            engine.render(PromptEngine.SIMPLE, "Hello {{ name }}", mapOf("bad-name" to "Ada"))
        }
    }

    @Test
    fun `rejects an engine that has no renderer`() {
        val simpleOnly = PromptTemplateEngine(listOf(SimplePromptRenderer()))

        assertFailsWith<InvalidPromptTemplateRequestException> {
            simpleOnly.validate(PromptEngine.JINJA2, "Hello")
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            simpleOnly.render(PromptEngine.JINJA2, "Hello", emptyMap())
        }
    }
}
