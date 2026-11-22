package com.agentgo.core.tool

/**
 * A tool the model is allowed to request.
 *
 * @property name stable tool name
 * @property description what the model should use the tool for
 * @property kind whether the tool is an ordinary tool or a CLI command
 */
data class ToolSpec(
    val name: String,
    val description: String,
    val kind: ToolKind = ToolKind.TOOL,
) {
    init {
        require(name.isNotBlank()) { "Tool name must not be blank." }
    }
}
