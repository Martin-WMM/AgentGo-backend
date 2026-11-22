package com.agentgo.cli.runner

import com.agentgo.cli.exception.CliFailedToStartException
import com.agentgo.cli.properties.CliProperties
import com.agentgo.dto.cli.CliExecutionStatus
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultCliProcessRunnerTest {
    private val runner = DefaultCliProcessRunner(
        CliProperties(
            timeoutMs = 10_000,
            maxStdoutChars = 200_000,
            maxStderrChars = 200_000,
        ),
    )

    @Test
    fun `runs script and captures stdout`() {
        val outcome = runner.run(echoScript("AGENTGO_CLI_OK"), emptyMap())

        assertEquals(CliExecutionStatus.COMPLETED, outcome.status)
        assertEquals(0, outcome.exitCode)
        assertTrue(outcome.stdout.contains("AGENTGO_CLI_OK"))
        assertTrue(outcome.durationMs >= 0)
    }

    @Test
    fun `applies environment variables`() {
        val outcome = runner.run(echoEnvScript("CLI_ENV_VAR"), mapOf("CLI_ENV_VAR" to "from-map"))

        assertEquals(CliExecutionStatus.COMPLETED, outcome.status)
        assertEquals(0, outcome.exitCode)
        assertTrue(outcome.stdout.contains("from-map"))
    }

    @Test
    fun `truncates stdout to configured limit`() {
        val limitedRunner = DefaultCliProcessRunner(
            CliProperties(timeoutMs = 10_000, maxStdoutChars = 5, maxStderrChars = 5),
        )

        val outcome = limitedRunner.run(echoScript("abcdefghijklmnop"), emptyMap())

        assertEquals(CliExecutionStatus.COMPLETED, outcome.status)
        assertEquals(5, outcome.stdout.length)
    }

    @Test
    fun `returns timed out status when execution exceeds limit`() {
        val quickRunner = DefaultCliProcessRunner(
            CliProperties(timeoutMs = 300, maxStdoutChars = 10_000, maxStderrChars = 10_000),
        )

        val outcome = quickRunner.run(sleepScript(), emptyMap())

        assertEquals(CliExecutionStatus.TIMED_OUT, outcome.status)
    }

    @Test
    fun `truncates stderr to configured limit`() {
        val limitedRunner = DefaultCliProcessRunner(
            CliProperties(timeoutMs = 10_000, maxStdoutChars = 200_000, maxStderrChars = 4),
        )

        val outcome = limitedRunner.run(stderrScript("abcdefghijklmnop"), emptyMap())

        assertEquals(CliExecutionStatus.COMPLETED, outcome.status)
        assertEquals(4, outcome.stderr.length)
    }

    @Test
    fun `throws when process starter fails with IOException`() {
        val failingRunner = DefaultCliProcessRunner(
            properties = CliProperties(timeoutMs = 1_000, maxStdoutChars = 100, maxStderrChars = 100),
            processStarter = { throw IOException("spawn failed") },
        )

        val exception = assertFailsWith<CliFailedToStartException> {
            failingRunner.run(echoScript("unused"), emptyMap())
        }

        assertEquals("CLI-002", exception.responseType)
        assertTrue(exception.cause is IOException)
    }

    @Test
    fun `decodes windows console bytes with the native charset instead of utf8`() {
        if (!isWindows()) return

        // cmd prints Chinese "不是内部或外部命令..." in the OEM/native charset when a
        // missing command is invoked. Decoding as UTF-8 would produce U+FFFD glyphs.
        val outcome = runner.run("agentgo-missing-command-xyz", emptyMap())

        assertEquals(CliExecutionStatus.COMPLETED, outcome.status)
        assertTrue(outcome.exitCode != 0)
        assertFalse(outcome.stderr.contains('\uFFFD'))
        assertTrue(
            outcome.stderr.contains("不是内部或外部命令") ||
                outcome.stderr.contains("不是外部命令") ||
                outcome.stderr.lowercase().contains("not recognized") ||
                outcome.stderr.contains("agentgo-missing-command-xyz"),
        )
    }

    private fun isWindows(): Boolean = System.getProperty("os.name").lowercase().contains("win")

    private fun echoScript(text: String): String = "echo $text"

    private fun echoEnvScript(variable: String): String =
        if (isWindows()) {
            "echo %$variable%"
        } else {
            "echo \$$variable"
        }

    private fun sleepScript(): String =
        if (isWindows()) {
            "ping -n 6 127.0.0.1 >nul"
        } else {
            "sleep 5"
        }

    private fun stderrScript(text: String): String = "echo $text 1>&2"
}
