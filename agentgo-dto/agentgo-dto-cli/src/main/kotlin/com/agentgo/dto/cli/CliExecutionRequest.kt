package com.agentgo.dto.cli

import com.agentgo.commons.validation.TrimmedNotBlank
import com.agentgo.commons.validation.TrimmedSize
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Request body for POST /api/v1/cli/executions.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 */
@Schema(
    description = "Request body for POST /api/v1/cli/executions. Runs a one-shot shell script " +
        "for the authenticated user. Optional environment variables are applied to the process.",
)
class CliExecutionRequest {
    @field:TrimmedNotBlank
    @field:TrimmedSize(min = 1, max = 10000)
    @field:Schema(
        description = "Shell script or command to execute",
        example = "echo hello",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 10000,
    )
    var script: String = ""

    @field:Schema(
        description = "Optional environment variables applied to the child process",
        example = """{"LANG":"en_US.UTF-8"}""",
    )
    var environment: Map<String, String>? = null
}