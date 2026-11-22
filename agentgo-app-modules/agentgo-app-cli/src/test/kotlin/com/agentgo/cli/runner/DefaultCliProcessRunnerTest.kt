package com.agentgo.cli.runner

import com.agentgo.cli.exception.CliFailedToStartException
import com.agentgo.cli.properties.CliProperties
import com.agentgo.dto.cli.CliExecutionStatus
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset
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
        val charset = DefaultCliProcessRunner.platformOutputCharset()
        val limitedRunner = DefaultCliProcessRunner(
            properties = CliProperties(timeoutMs = 10_000, maxStdoutChars = 200_000, maxStderrChars = 4),
            processStarter = {
                object : Process() {
                    override fun getOutputStream(): OutputStream = OutputStream.nullOutputStream()
                    override fun getInputStream() = ByteArrayInputStream(ByteArray(0))
                    override fun getErrorStream() =
                        ByteArrayInputStream("abcdefghijklmnop".toByteArray(charset))
                    override fun waitFor(): Int = 0
                    override fun exitValue(): Int = 0
                    override fun destroy() = Unit
                }
            },
        )

        val outcome = limitedRunner.run(echoScript("unused"), emptyMap())

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
    fun `rejects executables outside the fixed map before spawn`() {
        val exception = assertFailsWith<CliFailedToStartException> {
            runner.run("rm -rf /tmp/x", emptyMap())
        }
        assertTrue(exception.message.contains("not permitted"))
    }

    @Test
    fun `windows date uses slash t so it does not wait for interactive input`() {
        if (!isWindows()) return

        var launched: List<String> = emptyList()
        val capturingRunner = DefaultCliProcessRunner(
            properties = CliProperties(timeoutMs = 5_000, maxStdoutChars = 10_000, maxStderrChars = 10_000),
            processStarter = { builder ->
                launched = builder.command()
                ProcessBuilder("cmd.exe", "/c", "echo ok").start()
            },
        )

        val outcome = capturingRunner.run("date", emptyMap())

        assertEquals(CliExecutionStatus.COMPLETED, outcome.status)
        assertTrue(launched.take(3) == listOf("cmd.exe", "/c", "date") || launched.contains("date"))
        assertTrue(launched.any { it.equals("/t", ignoreCase = true) })
    }

    @Test
    fun `windows date completes quickly without timing out`() {
        if (!isWindows()) return

        val outcome = runner.run("date", emptyMap())

        assertEquals(CliExecutionStatus.COMPLETED, outcome.status)
        assertEquals(0, outcome.exitCode)
        assertTrue(outcome.stdout.isNotBlank())
        assertTrue(outcome.durationMs < 5_000)
    }

    @Test
    fun `decodes windows console bytes with the native charset instead of utf8`() {
        if (!isWindows()) return

        val native = DefaultCliProcessRunner.platformOutputCharset()
        val gbkMessage = "不是内部或外部命令"
        val runnerWithMissing = DefaultCliProcessRunner(
            properties = CliProperties(timeoutMs = 10_000, maxStdoutChars = 10_000, maxStderrChars = 10_000),
            fixedExecutables = mapOf("nosuchcli" to "nosuchcli-agentgo-xyz"),
            processStarter = {
                object : Process() {
                    override fun getOutputStream(): OutputStream = OutputStream.nullOutputStream()
                    override fun getInputStream() = ByteArrayInputStream(ByteArray(0))
                    override fun getErrorStream() =
                        ByteArrayInputStream(gbkMessage.toByteArray(native))
                    override fun waitFor(): Int = 1
                    override fun exitValue(): Int = 1
                    override fun destroy() = Unit
                }
            },
        )

        val outcome = runnerWithMissing.run("nosuchcli", emptyMap())

        assertEquals(CliExecutionStatus.COMPLETED, outcome.status)
        assertTrue(outcome.exitCode != 0)
        assertFalse(outcome.stderr.contains('\uFFFD'))
        assertTrue(outcome.stderr.contains(gbkMessage))
        // Prove UTF-8 would have corrupted the same bytes on a non-UTF8 native charset.
        if (!native.name().equals("UTF-8", ignoreCase = true)) {
            val mojibake = String(gbkMessage.toByteArray(native), Charset.forName("UTF-8"))
            assertTrue(mojibake.contains('\uFFFD') || mojibake != gbkMessage)
        }
    }

    private fun isWindows(): Boolean = System.getProperty("os.name").lowercase().contains("win")

    private fun echoScript(text: String): String = "echo $text"

    private fun echoEnvScript(variable: String): String =
        if (isWindows()) {
            // cmd.exe expands %VAR% for the echo builtin after /c.
            "echo %$variable%"
        } else {
            // No shell expansion: printenv reads the process environment directly.
            "printenv $variable"
        }

    private fun sleepScript(): String =
        if (isWindows()) {
            "ping -n 6 127.0.0.1"
        } else {
            "sleep 5"
        }
}
