package com.agentgo.prompts.render

import com.agentgo.prompts.exception.InvalidPromptTemplateRequestException

/**
 * Checks that render variables are a JSON-compatible object with identifier keys.
 */
object PromptVariableValidator {
    private val identifier = Regex("[A-Za-z_][A-Za-z0-9_]*")

    /**
     * Returns [variables] when every value is a JSON scalar, array, or object.
     *
     * Top-level keys must be identifiers so both template engines can reference them.
     *
     * @param variables caller-supplied variable map
     * @return the same map when it is valid
     * @throws InvalidPromptTemplateRequestException when a key or value is not allowed
     */
    fun requireVariables(variables: Map<String, Any?>): Map<String, Any?> {
        if (variables.size > PromptTemplateLimits.VARIABLE_COLLECTION_MAX_SIZE) {
            throw InvalidPromptTemplateRequestException("The prompt template variables are too large.")
        }
        variables.forEach { (key, value) ->
            if (!identifier.matches(key)) {
                throw InvalidPromptTemplateRequestException("The prompt template variable name is invalid.")
            }
            requireJsonValue(value, 1)
        }
        return variables
    }

    private fun requireJsonValue(value: Any?, depth: Int) {
        if (depth > PromptTemplateLimits.VARIABLE_MAX_DEPTH) {
            throw InvalidPromptTemplateRequestException("The prompt template variables are nested too deeply.")
        }
        when (value) {
            null, is Boolean -> Unit
            is String -> {
                if (value.length > PromptTemplateLimits.VARIABLE_STRING_MAX_LENGTH) {
                    throw InvalidPromptTemplateRequestException("A prompt template variable is too long.")
                }
            }
            is Number -> {
                if (value is Double && !value.isFinite() || value is Float && !value.isFinite()) {
                    throw InvalidPromptTemplateRequestException("A prompt template variable number is invalid.")
                }
            }
            is List<*> -> {
                if (value.size > PromptTemplateLimits.VARIABLE_COLLECTION_MAX_SIZE) {
                    throw InvalidPromptTemplateRequestException("The prompt template variables are too large.")
                }
                value.forEach { item -> requireJsonValue(item, depth + 1) }
            }
            is Map<*, *> -> {
                if (value.size > PromptTemplateLimits.VARIABLE_COLLECTION_MAX_SIZE) {
                    throw InvalidPromptTemplateRequestException("The prompt template variables are too large.")
                }
                value.forEach { (key, item) ->
                    if (key !is String || key.isBlank()) {
                        throw InvalidPromptTemplateRequestException("The prompt template variable name is invalid.")
                    }
                    requireJsonValue(item, depth + 1)
                }
            }
            else -> throw InvalidPromptTemplateRequestException("The prompt template variables must be JSON values.")
        }
    }
}
