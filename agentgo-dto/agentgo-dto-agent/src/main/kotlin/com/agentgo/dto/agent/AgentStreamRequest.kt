package com.agentgo.dto.agent

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

/**
 * Request body for POST /api/v1/agent/stream.
 *
 * Mutable JavaBean properties let Spring Boot 4 / Jackson 3 deserialize the body
 * without a Jackson Kotlin module.
 */
@Schema(
    description = "Request body for POST /api/v1/agent/stream. Starts one agent run and " +
        "streams lifecycle, message, tool, and CLI events.",
)
class AgentStreamRequest {
    @field:Valid
    @field:NotNull
    @field:Schema(description = "Graph state for this run", requiredMode = Schema.RequiredMode.REQUIRED)
    var state: AgentStreamState = AgentStreamState()

    @field:Valid
    @field:NotNull
    @field:Schema(
        description = "Run configuration. threadId is required.",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var config: AgentStreamConfig = AgentStreamConfig()

    @field:NotNull
    @field:Schema(
        description = "Header parameters associated with this run",
        example = """{"Accept-Language":"zh-CN"}""",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var headers: Map<String, String> = emptyMap()

    @field:NotNull
    @field:Schema(
        description = "Caller metadata stored with the run",
        example = """{"source":"ui"}""",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var metadata: Map<String, String> = emptyMap()
}
