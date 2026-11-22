package com.agentgo.dto.agent

import com.agentgo.commons.validation.TrimmedNotBlank
import com.agentgo.commons.validation.TrimmedSize
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Run configuration for the agent stream.
 *
 * [threadId] is required and identifies the conversation thread.
 */
@Schema(description = "Run configuration. threadId is required.")
class AgentStreamConfig {
    @field:TrimmedNotBlank
    @field:TrimmedSize(min = 1, max = 200)
    @field:Schema(
        description = "Conversation thread id. Required.",
        example = "thread-7f3a",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 200,
    )
    var threadId: String = ""
}
