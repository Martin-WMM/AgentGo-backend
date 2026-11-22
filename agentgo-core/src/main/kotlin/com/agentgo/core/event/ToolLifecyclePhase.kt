package com.agentgo.core.event

/**
 * Lifecycle of one tool call.
 */
enum class ToolLifecyclePhase {
    STARTED,
    RUNNING,
    SUCCEEDED,
    FAILED,
}
