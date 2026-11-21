package com.agentgo.prompts.render

import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.prompts.exception.InvalidPromptTemplateRequestException
import com.agentgo.prompts.exception.PromptTemplateRenderException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class Jinja2PromptRendererTest {
    private val renderer = Jinja2PromptRenderer()

    @Test
    fun `renders variables conditionals and loops`() {
        val output = renderer.render(
            "{% if ready %}Hello {{ name }}{% endif %} {% for tag in tags %}{{ tag }}{% endfor %}",
            mapOf("ready" to true, "name" to "Ada", "tags" to listOf("a", "b")),
        )

        assertEquals(PromptEngine.JINJA2, renderer.engine)
        assertTrue(output.contains("Hello Ada"))
        assertTrue(output.contains("a"))
        assertTrue(output.contains("b"))
    }

    @Test
    fun `validate accepts a template before variables are supplied`() {
        renderer.validate("Hello {{ name }}")
    }

    @Test
    fun `rejects external template tags and broken syntax`() {
        assertFailsWith<InvalidPromptTemplateRequestException> {
            renderer.validate("{% include 'secret.txt' %}")
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            renderer.validate("{% import 'other.html' as other %}")
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            renderer.validate("{% from 'macros.html' import greet %}")
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            renderer.validate("{% extends 'base.html' %}")
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            renderer.validate("Hello {{ name")
        }
    }

    @Test
    fun `render fails when a referenced variable is missing`() {
        assertFailsWith<PromptTemplateRenderException> {
            renderer.render("Hello {{ missing }}", emptyMap())
        }
    }

    @Test
    fun `render rejects an external include even if validation was skipped`() {
        assertFailsWith<InvalidPromptTemplateRequestException> {
            renderer.render("{%- include 'secret.txt' %}", emptyMap())
        }
    }
}
