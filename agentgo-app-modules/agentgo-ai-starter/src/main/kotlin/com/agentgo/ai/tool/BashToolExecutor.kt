package com.agentgo.ai.tool

import com.agentgo.cli.exception.CliException
import com.agentgo.cli.service.CliExecutionService
import com.agentgo.core.json.AgentJson
import com.agentgo.core.tool.PendingToolCall
import com.agentgo.core.tool.ToolExecutor
import com.agentgo.core.tool.ToolResult
import com.agentgo.core.tool.ToolRuntimeContext
import com.agentgo.dto.cli.CliExecutionRequest
import com.agentgo.dto.cli.CliExecutionResponse
import com.agentgo.dto.cli.CliExecutionStatus
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Agent tool boundary that runs Bash / CLI calls through [CliExecutionService].
 *
 * This is the in-process equivalent of `POST /api/v1/cli/executions`: the same
 * validation, process runner, and audit trail apply. The process environment is
 * built from [ToolRuntimeContext] (user, token, locale, thread, metadata) with
 * optional model extras layered underneath.
 *
 * When [BashTool.ARG_CODE] / [BashTool.ARG_SOURCE] is present, the source is
 * written to a temporary `.py` file and executed with `python3`.
 *
 * @property cliExecutionService authenticated CLI execution application service
 */
class BashToolExecutor(
    private val cliExecutionService: CliExecutionService,
) : ToolExecutor {
    override fun execute(call: PendingToolCall, context: ToolRuntimeContext): ToolResult {
        if (call.name != BashTool.NAME) {
            return ToolResult(
                name = call.name,
                success = false,
                output = "",
                error = "Unknown tool: ${call.name}",
            )
        }

        var tempFile: Path? = null
        return try {
            val script = resolveScript(call) { tempFile = it }
            if (script.isBlank()) {
                return ToolResult(
                    name = BashTool.NAME,
                    success = false,
                    output = "",
                    error = "Missing required argument: ${BashTool.ARG_SCRIPT}",
                )
            }

            val request =
                CliExecutionRequest().apply {
                    this.script = script
                    this.environment =
                        RuntimeContextEnvironment.build(
                            context = context,
                            extras = parseEnvironment(call.arguments[BashTool.ARG_ENVIRONMENT]),
                        )
                }

            BashToolSecuritySupport.withAuthenticatedContext(context) {
                toToolResult(cliExecutionService.execute(request))
            }
        } catch (exception: CliException) {
            ToolResult(
                name = BashTool.NAME,
                success = false,
                output = "",
                error = exception.message,
            )
        } finally {
            tempFile?.let { Files.deleteIfExists(it) }
        }
    }

    /**
     * Builds the CLI script line. When Python source is provided, materializes a
     * temp `.py` file and returns `python3 <path>` (or `python <path>`).
     */
    private fun resolveScript(call: PendingToolCall, onTempFile: (Path) -> Unit): String {
        val script =
            call.arguments[BashTool.ARG_SCRIPT]?.trim().orEmpty().ifBlank {
                call.arguments["command"]?.trim().orEmpty()
            }
        val code =
            call.arguments[BashTool.ARG_CODE]?.trim().orEmpty().ifBlank {
                call.arguments[BashTool.ARG_SOURCE]?.trim().orEmpty()
            }
        if (code.isBlank()) {
            return script
        }

        val interpreter = resolvePythonInterpreter(script)
        val file = Files.createTempFile("agentgo-py-", ".py")
        Files.writeString(file, code, StandardCharsets.UTF_8)
        onTempFile(file)
        return "$interpreter ${quotePath(file.toAbsolutePath().toString())}"
    }

    private fun resolvePythonInterpreter(script: String): String {
        val primary = script.trim().substringBefore(' ').trim().lowercase()
        return when (primary) {
            "python", "python.exe" -> "python"
            else -> "python3"
        }
    }

    private fun quotePath(path: String): String =
        if (path.any { it.isWhitespace() }) "\"$path\"" else path

    private fun parseEnvironment(raw: String?): Map<String, String>? {
        if (raw.isNullOrBlank()) return null
        val parsed = AgentJson.readArguments(raw)
        return parsed.ifEmpty { null }
    }

    private fun toToolResult(response: CliExecutionResponse): ToolResult {
        val output = formatOutput(response)
        val success =
            response.status == CliExecutionStatus.COMPLETED && response.exitCode == 0
        return if (success) {
            ToolResult(
                name = BashTool.NAME,
                success = true,
                output = output,
                error = null,
            )
        } else {
            // ExecutionNode surfaces [ToolResult.error] to the model on failure.
            ToolResult(
                name = BashTool.NAME,
                success = false,
                output = output,
                error = output,
            )
        }
    }

    private fun formatOutput(response: CliExecutionResponse): String =
        buildString {
            append("status=").append(response.status.name)
            append(" exitCode=").append(response.exitCode)
            append(" durationMs=").append(response.durationMs)
            append('\n')
            append("stdout:\n")
            append(response.stdout)
            if (response.stderr.isNotEmpty()) {
                append("\nstderr:\n")
                append(response.stderr)
            }
        }
}
