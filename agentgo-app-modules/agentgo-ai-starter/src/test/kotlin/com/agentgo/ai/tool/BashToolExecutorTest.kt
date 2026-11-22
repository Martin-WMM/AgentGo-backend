package com.agentgo.ai.tool

import com.agentgo.cli.exception.ForbiddenCliCommandException
import com.agentgo.cli.service.CliExecutionService
import com.agentgo.core.tool.PendingToolCall
import com.agentgo.core.tool.ToolRuntimeContext
import com.agentgo.dto.cli.CliExecutionRequest
import com.agentgo.dto.cli.CliExecutionResponse
import com.agentgo.dto.cli.CliExecutionStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BashToolExecutorTest {
    private val cliExecutionService = mock<CliExecutionService>()
    private val executor = BashToolExecutor(cliExecutionService)

    @Test
    fun `bash tool catalog uses the CLI kind`() {
        assertEquals("bash", BashTool.NAME)
        assertEquals(BashTool.NAME, BashTool.SPEC.name)
        assertEquals(com.agentgo.core.tool.ToolKind.CLI, BashTool.SPEC.kind)
    }

    @Test
    fun `execute rejects unknown tool names`() {
        val result = executor.execute(PendingToolCall("lookup", emptyMap()), ToolRuntimeContext.EMPTY)

        assertFalse(result.success)
        assertEquals("Unknown tool: lookup", result.error)
    }

    @Test
    fun `execute requires a script argument`() {
        val result = executor.execute(PendingToolCall(BashTool.NAME, emptyMap()), ToolRuntimeContext.EMPTY)

        assertFalse(result.success)
        assertEquals("Missing required argument: script", result.error)
    }

    @Test
    fun `execute injects runtime context into the CLI environment`() {
        whenever(cliExecutionService.execute(any())).thenReturn(
            CliExecutionResponse(
                exitCode = 0,
                stdout = "hello\n",
                stderr = "",
                durationMs = 12,
                status = CliExecutionStatus.COMPLETED,
            ),
        )
        val context =
            ToolRuntimeContext(
                runId = "run-1",
                threadId = "thread-1",
                headers = mapOf(
                    "Accept-Language" to "zh-CN",
                    "Authorization" to "Bearer secret-token",
                ),
                metadata = mapOf(
                    "ownerUserId" to "user-ada",
                    "sessionId" to "42",
                    "source" to "ui",
                    "workspaceId" to "ws-1",
                ),
            )

        val result =
            executor.execute(
                PendingToolCall(
                    BashTool.NAME,
                    mapOf(
                        "script" to "echo hello",
                        "environment" to """{"FOO":"bar","AGENTGO_USER_ID":"spoofed"}""",
                    ),
                ),
                context,
            )

        val captor = argumentCaptor<CliExecutionRequest>()
        verify(cliExecutionService).execute(captor.capture())
        val env = captor.firstValue.environment!!
        assertEquals("echo hello", captor.firstValue.script)
        assertEquals("bar", env["FOO"])
        assertEquals("user-ada", env[RuntimeContextEnvironment.USER_ID])
        assertEquals("run-1", env[RuntimeContextEnvironment.RUN_ID])
        assertEquals("thread-1", env[RuntimeContextEnvironment.THREAD_ID])
        assertEquals("42", env[RuntimeContextEnvironment.SESSION_ID])
        assertEquals("ui", env[RuntimeContextEnvironment.SOURCE])
        assertEquals("zh-CN", env[RuntimeContextEnvironment.LOCALE])
        assertEquals("zh_CN.UTF-8", env[RuntimeContextEnvironment.LANG])
        assertEquals("secret-token", env[RuntimeContextEnvironment.TOKEN])
        assertEquals("ws-1", env["AGENTGO_META_WORKSPACEID"])
        assertTrue(result.success)
        assertNull(result.error)
    }

    @Test
    fun `execute accepts command as a script alias`() {
        whenever(cliExecutionService.execute(any())).thenReturn(
            CliExecutionResponse(
                exitCode = 0,
                stdout = "ok",
                stderr = "",
                durationMs = 1,
                status = CliExecutionStatus.COMPLETED,
            ),
        )

        val result =
            executor.execute(
                PendingToolCall(BashTool.NAME, mapOf("command" to "echo ok")),
                ToolRuntimeContext.EMPTY,
            )

        val captor = argumentCaptor<CliExecutionRequest>()
        verify(cliExecutionService).execute(captor.capture())
        assertEquals("echo ok", captor.firstValue.script)
        assertTrue(result.success)
    }

    @Test
    fun `execute materializes python code into a temp script`() {
        whenever(cliExecutionService.execute(any())).thenReturn(
            CliExecutionResponse(
                exitCode = 0,
                stdout = "832040\n",
                stderr = "",
                durationMs = 8,
                status = CliExecutionStatus.COMPLETED,
            ),
        )

        val result =
            executor.execute(
                PendingToolCall(
                    BashTool.NAME,
                    mapOf(
                        "script" to "python3",
                        "code" to "print(832040)",
                    ),
                ),
                ToolRuntimeContext.EMPTY,
            )

        val captor = argumentCaptor<CliExecutionRequest>()
        verify(cliExecutionService).execute(captor.capture())
        assertTrue(captor.firstValue.script.startsWith("python3 "))
        assertTrue(captor.firstValue.script.contains(".py"))
        assertTrue(result.success)
        assertTrue(result.output.contains("832040"))
    }

    @Test
    fun `execute maps non zero exit codes to tool failure`() {
        whenever(cliExecutionService.execute(any())).thenReturn(
            CliExecutionResponse(
                exitCode = 2,
                stdout = "",
                stderr = "boom",
                durationMs = 3,
                status = CliExecutionStatus.COMPLETED,
            ),
        )

        val result =
            executor.execute(
                PendingToolCall(BashTool.NAME, mapOf("script" to "false")),
                ToolRuntimeContext.EMPTY,
            )

        assertFalse(result.success)
        assertTrue(result.error!!.contains("stderr:\nboom"))
        assertTrue(result.output.contains("exitCode=2"))
    }

    @Test
    fun `execute maps CLI policy exceptions to tool failure`() {
        whenever(cliExecutionService.execute(any())).thenThrow(
            ForbiddenCliCommandException("The CLI command \"rm\" is not allowed."),
        )

        val result =
            executor.execute(
                PendingToolCall(BashTool.NAME, mapOf("script" to "rm -rf /")),
                ToolRuntimeContext.EMPTY,
            )

        assertFalse(result.success)
        assertEquals("The CLI command \"rm\" is not allowed.", result.error)
    }
}
