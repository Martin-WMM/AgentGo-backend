package com.agentgo.prompts.dto

import com.agentgo.dto.presentation.IconSource
import com.agentgo.dto.presentation.ResourceIcon
import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.dto.prompts.PromptTemplateCreateRequest
import com.agentgo.dto.prompts.PromptTemplateMetadata
import com.agentgo.dto.prompts.PromptTemplatePreviewRequest
import com.agentgo.dto.prompts.PromptTemplateUpdateRequest
import jakarta.validation.Validation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PromptTemplateRequestValidationTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `create accepts trimmed text an omitted commit message and a palette color`() {
        val request = PromptTemplateCreateRequest().apply {
            groupName = "  chat  "
            name = "  Greeting  "
            engine = PromptEngine.SIMPLE
            content = "  Hello {{ name }}  "
            description = "  "
            message = "   "
            tags = listOf(" draft ", "draft", " ")
            metadata = PromptTemplateMetadata().apply { color = "#0e7490" }
        }

        assertTrue(validator.validate(request).isEmpty())
    }

    @Test
    fun `create rejects a blank name an unknown color a bad icon and too many tags`() {
        val request = PromptTemplateCreateRequest().apply {
            groupName = " "
            name = " "
            content = " "
            description = "x".repeat(2001)
            tags = List(21) { "tag$it" }
            metadata = PromptTemplateMetadata().apply {
                color = "#000000"
                icon = ResourceIcon().apply {
                    source = IconSource.ICONIFY
                    value = "Not Icon"
                }
            }
        }

        val paths = validator.validate(request).map { it.propertyPath.toString() }.toSet()

        assertEquals(
            setOf(
                "groupName",
                "name",
                "engine",
                "content",
                "description",
                "tags",
                "metadata.color",
                "metadata.icon",
            ),
            paths,
        )
    }

    @Test
    fun `update requires a commit message and preview rejects blank content`() {
        val update = PromptTemplateUpdateRequest().apply {
            groupName = "chat"
            name = "Greeting"
            engine = PromptEngine.SIMPLE
            content = "Hello"
            message = " "
        }
        val preview = PromptTemplatePreviewRequest().apply {
            engine = PromptEngine.SIMPLE
            content = "   "
        }

        assertEquals(setOf("message"), validator.validate(update).map { it.propertyPath.toString() }.toSet())
        assertEquals(setOf("content"), validator.validate(preview).map { it.propertyPath.toString() }.toSet())
    }
}
