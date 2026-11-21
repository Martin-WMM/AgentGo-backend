package com.agentgo.prompts.render

import com.agentgo.prompts.exception.InvalidPromptTemplateRequestException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PromptVariableValidatorTest {
    @Test
    fun `accepts nested json values`() {
        val variables = mapOf(
            "user" to mapOf("name" to "Ada"),
            "tags" to listOf("a", 1, true, null),
        )

        assertEquals(variables, PromptVariableValidator.requireVariables(variables))
    }

    @Test
    fun `rejects invalid names non json values oversized collections and deep nesting`() {
        assertFailsWith<InvalidPromptTemplateRequestException> {
            PromptVariableValidator.requireVariables(mapOf("bad-name" to "Ada"))
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            PromptVariableValidator.requireVariables(mapOf("value" to Any()))
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            PromptVariableValidator.requireVariables(
                mapOf("values" to List(PromptTemplateLimits.VARIABLE_COLLECTION_MAX_SIZE + 1) { "x" }),
            )
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            PromptVariableValidator.requireVariables(mapOf("name" to "x".repeat(PromptTemplateLimits.VARIABLE_STRING_MAX_LENGTH + 1)))
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            PromptVariableValidator.requireVariables(mapOf("value" to Double.POSITIVE_INFINITY))
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            PromptVariableValidator.requireVariables(nested(PromptTemplateLimits.VARIABLE_MAX_DEPTH + 1))
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            PromptVariableValidator.requireVariables(mapOf("group" to mapOf(1 to "Ada")))
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            PromptVariableValidator.requireVariables(
                (1..PromptTemplateLimits.VARIABLE_COLLECTION_MAX_SIZE + 1).associate { "k$it" to "v" },
            )
        }
    }

    private fun nested(depth: Int): Map<String, Any?> {
        var value: Any = "leaf"
        repeat(depth) {
            value = mapOf("child" to value)
        }
        return mapOf("root" to value)
    }
}
