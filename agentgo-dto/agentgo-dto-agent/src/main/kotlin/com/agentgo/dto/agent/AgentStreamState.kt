package com.agentgo.dto.agent

import com.agentgo.commons.validation.TrimmedNotBlank
import com.agentgo.commons.validation.TrimmedSize
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Graph state submitted to the agent stream.
 *
 * Mutable JavaBean properties let Spring Boot 4 / Jackson 3 deserialize the body
 * without a Jackson Kotlin module.
 */
@Schema(description = "State for one agent stream run")
class AgentStreamState {
    @field:TrimmedNotBlank
    @field:TrimmedSize(min = 1, max = 100000)
    @field:Schema(
        description = "User text that starts this run",
        example = "Summarize the open tasks",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 100000,
    )
    var input: String = ""
}
