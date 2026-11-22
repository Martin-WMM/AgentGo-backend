package com.agentgo.core.model

import com.agentgo.core.json.AgentJson
import com.fasterxml.jackson.databind.JsonNode
import java.io.IOException

/**
 * Reads the JSON decision protocol used by the AgentGo LLM node.
 *
 * A tool decision is `{"type":"tool","name":"...","arguments":{...}}`.
 * A final decision is `{"type":"final","content":"..."}`.
 * Prose that is not that object is treated as a final answer.
 */
object LlmDecisionParser {
    private val fence: Regex = Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)

    /**
     * Parses [content] into a tool call or a final answer.
     *
     * @param content raw assistant text
     * @return the decision
     */
    fun parse(content: String): LlmDecision {
        val trimmed = content.trim()
        val payload = jsonObject(trimmed) ?: return LlmDecision.Final(trimmed)
        val node = try {
            AgentJson.mapper.readTree(payload)
        } catch (ignored: IOException) {
            return LlmDecision.Final(trimmed)
        }
        if (!node.isObject) {
            return LlmDecision.Final(trimmed)
        }
        return when (node.path("type").asText("")) {
            "tool" -> toolCall(node, trimmed)
            "final" -> {
                val answer = node.path("content").asText("")
                if (answer.isBlank()) LlmDecision.Final(trimmed) else LlmDecision.Final(answer)
            }
            else -> LlmDecision.Final(trimmed)
        }
    }

    private fun toolCall(node: JsonNode, fallback: String): LlmDecision {
        val name = node.path("name").asText("").trim()
        if (name.isEmpty()) {
            return LlmDecision.Final(fallback)
        }
        val arguments = linkedMapOf<String, String>()
        val argsNode = node.path("arguments")
        if (argsNode.isObject) {
            argsNode.fields().forEachRemaining { field ->
                val value = field.value
                arguments[field.key] = if (value.isValueNode) value.asText() else value.toString()
            }
        }
        return LlmDecision.ToolCall(name, arguments)
    }

    private fun jsonObject(text: String): String? {
        val fenced = fence.find(text)?.groupValues?.get(1)?.trim()
        val body = if (fenced.isNullOrBlank()) text else fenced
        val start = body.indexOf('{')
        val end = body.lastIndexOf('}')
        if (start < 0 || end <= start) {
            return null
        }
        return body.substring(start, end + 1)
    }
}
