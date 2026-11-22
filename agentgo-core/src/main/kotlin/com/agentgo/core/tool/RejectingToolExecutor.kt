package com.agentgo.core.tool

/**
 * Tool executor that refuses every call.
 *
 * Used when an agent has no configured side effects.
 */
class RejectingToolExecutor : ToolExecutor {
    override fun execute(call: PendingToolCall, context: ToolRuntimeContext): ToolResult =
        ToolResult(
            name = call.name,
            success = false,
            output = "",
            error = "Tool execution is not configured: ${call.name}",
        )
}
