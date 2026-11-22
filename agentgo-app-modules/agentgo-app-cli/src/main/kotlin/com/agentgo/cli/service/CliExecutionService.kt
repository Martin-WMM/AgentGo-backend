package com.agentgo.cli.service

import com.agentgo.dto.cli.CliExecutionRequest
import com.agentgo.dto.cli.CliExecutionResponse

/** Application service for authenticated one-shot CLI execution. */
interface CliExecutionService {
    /**
     * Runs [request.script] and returns captured process output.
     *
     * @param request script and optional environment variables
     * @return structured exit code and captured streams
     */
    fun execute(request: CliExecutionRequest): CliExecutionResponse
}