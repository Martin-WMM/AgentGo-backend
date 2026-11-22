package com.agentgo.core.event

/**
 * Lifecycle of one CLI tool call.
 */
enum class CliLifecyclePhase {
    STARTED,
    RUNNING,
    SUCCEEDED,
    FAILED,
}
