package com.agentgo.dto.cli

import io.swagger.v3.oas.annotations.media.Schema

/** Outcome of a one-shot CLI / shell execution. */
@Schema(description = "CLI execution completion status")
enum class CliExecutionStatus {
    @field:Schema(description = "The process exited before the configured timeout")
    COMPLETED,

    @field:Schema(description = "The process was forcibly stopped after the configured timeout")
    TIMED_OUT,

    @field:Schema(description = "The process could not be started")
    FAILED_TO_START,

    @field:Schema(description = "The script was rejected by the command security policy")
    REJECTED,
}