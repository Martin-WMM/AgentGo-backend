package com.agentgo.core.tool

/**
 * Normalized result of one tool call.
 *
 * @property name tool that was requested
 * @property success whether the call completed
 * @property output safe text to show the model when [success] is true
 * @property error safe failure text when [success] is false
 */
data class ToolResult(
    val name: String,
    val success: Boolean,
    val output: String,
    val error: String? = null,
) : java.io.Serializable
