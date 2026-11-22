package com.agentgo.cli.runner

import com.agentgo.dto.cli.CliExecutionStatus

/**
 * Executes a shell script and captures structured process output.
 */
interface CliProcessRunner {
    /**
     * Runs [script] with optional [environment] variables applied to the child process.
     *
     * @param script shell command or script body
     * @param environment optional environment variables
     * @return captured exit code, streams, duration, and completion status
     */
    fun run(script: String, environment: Map<String, String>): CliRunOutcome
}

/**
 * Structured output of a CLI process invocation.
 *
 * @property exitCode process exit code, or `-1` when unavailable
 * @property stdout captured standard output
 * @property stderr captured standard error
 * @property durationMs wall-clock duration in milliseconds
 * @property status whether the run completed or timed out
 */
data class CliRunOutcome(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val durationMs: Long,
    val status: CliExecutionStatus,
)