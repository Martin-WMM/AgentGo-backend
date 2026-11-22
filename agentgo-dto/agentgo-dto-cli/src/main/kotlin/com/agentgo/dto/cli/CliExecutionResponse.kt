package com.agentgo.dto.cli

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Structured result of a one-shot CLI / shell execution.
 *
 * @property exitCode process exit code, or `-1` when unavailable
 * @property stdout captured standard output, truncated to server limits
 * @property stderr captured standard error, truncated to server limits
 * @property durationMs wall-clock duration in milliseconds
 * @property status whether the run completed, timed out, or failed to start
 */
@Schema(description = "CLI execution result returned by POST /api/v1/cli/executions")
data class CliExecutionResponse(
    @field:Schema(description = "Process exit code", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    val exitCode: Int,
    @field:Schema(description = "Captured standard output", requiredMode = Schema.RequiredMode.REQUIRED)
    val stdout: String,
    @field:Schema(description = "Captured standard error", requiredMode = Schema.RequiredMode.REQUIRED)
    val stderr: String,
    @field:Schema(description = "Wall-clock duration in milliseconds", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
    val durationMs: Long,
    @field:Schema(description = "Execution outcome", example = "COMPLETED", requiredMode = Schema.RequiredMode.REQUIRED)
    val status: CliExecutionStatus,
)