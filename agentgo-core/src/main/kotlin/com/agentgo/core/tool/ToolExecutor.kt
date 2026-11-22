package com.agentgo.core.tool

/**
 * Controlled tool boundary used by the execution node.
 *
 * Implementations receive an already selected tool name, parsed arguments, and
 * the run's [ToolRuntimeContext]. They should treat context as the source of
 * identity, locale, and credentials for side effects such as CLI execution.
 */
fun interface ToolExecutor {
    /**
     * Runs one tool call.
     *
     * @param call tool name and arguments
     * @param context headers, metadata, and ids for the active run
     * @return normalized result
     */
    fun execute(call: PendingToolCall, context: ToolRuntimeContext): ToolResult
}
