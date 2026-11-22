package com.agentgo.cli.controller.apidoc

import com.agentgo.commons.api.ApiPaths
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.dto.cli.CliExecutionRequest
import com.agentgo.dto.cli.CliExecutionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Springdoc contract for authenticated CLI execution at `/api/v1/cli/executions`.
 *
 * Session cookie authentication is required. CSRF checks are skipped for this API prefix
 * so the SPA can POST JSON without a separate CSRF token header.
 */
@Tag(
    name = "CLI",
    description = "Authenticated one-shot shell execution at /api/v1/cli/executions. " +
        "Success code is 9300 CLI-EXECUTED. Invalid script uses CLI-001 (400). " +
        "Process start failures use CLI-002 (500). Busy workers use CLI-003 (503). " +
        "Blocked commands use CLI-004 (403). Missing authentication uses AUTH-002.",
)
@RequestMapping(ApiPaths.V1_CLI)
interface CliExecutionControllerApiDoc {
    @Operation(
        summary = "Execute a shell script",
        description = "Runs a one-shot shell command for the authenticated user. " +
            "Optional environment variables are applied to the child process. " +
            "Scripts are checked against the command allow/deny policy before start. " +
            "Returns 9300 CLI-EXECUTED on success.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "CLI execution completed", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid CLI script request"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "403", description = "CLI command blocked by policy"),
            ApiResponse(responseCode = "503", description = "CLI worker pool exhausted"),
            ApiResponse(responseCode = "500", description = "CLI process failed to start"),
        ],
    )
    @PostMapping("/executions")
    fun execute(@Valid @RequestBody request: CliExecutionRequest): CommonHttpResponse<CliExecutionResponse>
}