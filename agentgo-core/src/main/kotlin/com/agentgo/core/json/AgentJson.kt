package com.agentgo.core.json

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException

/**
 * Shared JSON codec for model payloads and tool arguments.
 */
object AgentJson {
    val mapper: ObjectMapper = ObjectMapper()

    /**
     * Serializes string arguments.
     *
     * @param arguments tool arguments
     * @return JSON object text
     */
    fun writeArguments(arguments: Map<String, String>): String = mapper.writeValueAsString(arguments)

    /**
     * Reads a JSON object of string arguments.
     *
     * Invalid JSON yields an empty map so a bad payload does not abort the graph.
     *
     * @param json JSON object text
     * @return argument map
     */
    fun readArguments(json: String): Map<String, String> {
        if (json.isBlank()) {
            return emptyMap()
        }
        return try {
            val node = mapper.readTree(json)
            if (!node.isObject) {
                return emptyMap()
            }
            val arguments = linkedMapOf<String, String>()
            node.fields().forEachRemaining { field ->
                val value = field.value
                arguments[field.key] = if (value.isValueNode) value.asText() else value.toString()
            }
            arguments
        } catch (ignored: IOException) {
            emptyMap()
        }
    }
}
