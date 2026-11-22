package com.agentgo.cli.controller

import com.agentgo.cli.controller.apidoc.CliExecutionControllerApiDoc
import com.agentgo.cli.service.CliExecutionService
import com.agentgo.commons.annotation.AgentGoLogApi
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.success.CliResponseCodeRegistry
import com.agentgo.dto.cli.CliExecutionRequest
import com.agentgo.dto.cli.CliExecutionResponse
import com.agentgo.starter.i18n.AgentGoMessages
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.RestController

/**
 * HTTP adapter for authenticated one-shot CLI execution.
 *
 * @property cliExecutionService CLI execution application service
 * @property messages localized API message resolver
 */
@RestController
class CliExecutionController(
    private val cliExecutionService: CliExecutionService,
    private val messages: AgentGoMessages,
) : CliExecutionControllerApiDoc {
    @AgentGoLogApi(name = "cli.execute")
    override fun execute(@Valid request: CliExecutionRequest): CommonHttpResponse<CliExecutionResponse> =
        CommonHttpResponse(
            code = CliResponseCodeRegistry.EXECUTED,
            responseType = CliResponseCodeRegistry.EXECUTED_TYPE,
            message = messages.get("cli.execution.completed", defaultMessage = "CLI execution completed"),
            data = cliExecutionService.execute(request),
        )
}