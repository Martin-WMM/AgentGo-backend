package com.agentgo.prompts.render

import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.prompts.exception.InvalidPromptTemplateRequestException
import com.agentgo.prompts.exception.PromptTemplateRenderException
import org.springframework.stereotype.Component

/**
 * Replaces `{{ identifier }}` placeholders and rejects expressions, filters, and control tags.
 */
@Component
class SimplePromptRenderer : PromptTemplateRenderer {
    override val engine: PromptEngine = PromptEngine.SIMPLE

    override fun validate(content: String) {
        scan(content) { "" }
    }

    override fun render(content: String, variables: Map<String, Any?>): String {
        val rendered = scan(content) { name -> stringify(name, variables) }
        if (rendered.length > PromptTemplateLimits.OUTPUT_MAX_LENGTH) {
            throw PromptTemplateRenderException()
        }
        return rendered
    }

    private fun scan(content: String, replace: (String) -> String): String {
        val rendered = StringBuilder(content.length)
        var index = 0
        while (index < content.length) {
            if (startsWith(content, index, OPEN)) {
                val end = content.indexOf(CLOSE, index + OPEN.length)
                if (end < 0) {
                    throw InvalidPromptTemplateRequestException("The prompt template has an unclosed placeholder.")
                }
                val name = content.substring(index + OPEN.length, end).trim()
                if (!IDENTIFIER.matches(name)) {
                    throw InvalidPromptTemplateRequestException("The prompt template placeholder is invalid.")
                }
                rendered.append(replace(name))
                index = end + CLOSE.length
            } else if (startsWith(content, index, CLOSE)) {
                throw InvalidPromptTemplateRequestException("The prompt template has an unexpected closing placeholder.")
            } else {
                rendered.append(content[index])
                index += 1
            }
        }
        return rendered.toString()
    }

    private fun stringify(name: String, variables: Map<String, Any?>): String {
        if (!variables.containsKey(name)) {
            throw PromptTemplateRenderException()
        }
        return when (val value = variables[name]) {
            is String, is Boolean -> value.toString()
            is Number -> {
                if (value is Double && !value.isFinite() || value is Float && !value.isFinite()) {
                    throw InvalidPromptTemplateRequestException("A prompt template variable number is invalid.")
                }
                value.toString()
            }
            else -> throw InvalidPromptTemplateRequestException(
                "A simple prompt template variable must be a string, number, or boolean.",
            )
        }
    }

    private fun startsWith(content: String, index: Int, token: String): Boolean =
        index + token.length <= content.length && content.regionMatches(index, token, 0, token.length)

    private companion object {
        const val OPEN: String = "{{"
        const val CLOSE: String = "}}"
        val IDENTIFIER: Regex = Regex("[A-Za-z_][A-Za-z0-9_]*")
    }
}
