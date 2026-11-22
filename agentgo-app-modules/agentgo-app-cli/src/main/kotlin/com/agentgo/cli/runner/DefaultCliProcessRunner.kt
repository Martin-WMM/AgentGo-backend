package com.agentgo.cli.runner

import com.agentgo.cli.exception.CliFailedToStartException
import com.agentgo.cli.properties.CliProperties
import com.agentgo.dto.cli.CliExecutionStatus
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Runs one-shot shell commands using the platform shell.
 *
 * Windows uses `cmd.exe /c` and decodes stdout/stderr with the JVM/OS native charset
 * (typically GBK on Chinese Windows) so console error text is not mojibake.
 * Other platforms use `bash -lc` with UTF-8.
 * Instances are owned by [com.agentgo.cli.pool.CliProcessWorker] slots in the startup pool.
 */
class DefaultCliProcessRunner(
    private val properties: CliProperties,
    private val processStarter: (ProcessBuilder) -> Process = { it.start() },
    private val outputCharset: Charset = platformOutputCharset(),
) : CliProcessRunner {
    override fun run(script: String, environment: Map<String, String>): CliRunOutcome {
        val startedAt = System.nanoTime()
        val processBuilder = buildProcessBuilder(script)
        processBuilder.environment().putAll(environment)

        val process =
            try {
                processStarter(processBuilder)
            } catch (exception: IOException) {
                throw CliFailedToStartException(cause = exception)
            }

        val stdoutCapture = StreamCapture(properties.maxStdoutChars, outputCharset)
        val stderrCapture = StreamCapture(properties.maxStderrChars, outputCharset)
        val stdoutThread = Thread { stdoutCapture.drain(process.inputStream) }
        val stderrThread = Thread { stderrCapture.drain(process.errorStream) }
        stdoutThread.start()
        stderrThread.start()

        val finished = process.waitFor(properties.timeoutMs, TimeUnit.MILLISECONDS)
        val durationMs = (System.nanoTime() - startedAt) / 1_000_000

        if (!finished) {
            process.destroyForcibly()
            process.waitFor(5, TimeUnit.SECONDS)
            joinQuietly(stdoutThread)
            joinQuietly(stderrThread)
            return CliRunOutcome(
                exitCode = exitCodeOrNegative(process),
                stdout = stdoutCapture.text(),
                stderr = stderrCapture.text(),
                durationMs = durationMs,
                status = CliExecutionStatus.TIMED_OUT,
            )
        }

        joinQuietly(stdoutThread)
        joinQuietly(stderrThread)

        return CliRunOutcome(
            exitCode = process.exitValue(),
            stdout = stdoutCapture.text(),
            stderr = stderrCapture.text(),
            durationMs = durationMs,
            status = CliExecutionStatus.COMPLETED,
        )
    }

    private fun buildProcessBuilder(script: String): ProcessBuilder {
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        return if (isWindows) {
            ProcessBuilder("cmd.exe", "/c", script)
        } else {
            ProcessBuilder("bash", "-lc", script)
        }
    }

    private fun exitCodeOrNegative(process: Process): Int =
        try {
            process.exitValue()
        } catch (_: IllegalThreadStateException) {
            -1
        }

    private fun joinQuietly(thread: Thread) {
        try {
            thread.join(2_000)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private class StreamCapture(
        private val maxChars: Int,
        private val charset: Charset,
    ) {
        private val buffer = StringBuilder()

        fun drain(stream: InputStream) {
            val chunk = ByteArray(8_192)
            try {
                while (true) {
                    val read = stream.read(chunk)
                    if (read <= 0) {
                        break
                    }
                    if (buffer.length >= maxChars) {
                        continue
                    }
                    val text = String(chunk, 0, read, charset)
                    val remaining = maxChars - buffer.length
                    if (text.length <= remaining) {
                        buffer.append(text)
                    } else {
                        buffer.append(text, 0, remaining)
                    }
                }
            } catch (_: IOException) {
                // Stream closed when the process exits; partial capture is acceptable.
            }
        }

        fun text(): String = buffer.toString()
    }

    companion object {
        /**
         * Chooses the charset used to decode shell stdout/stderr.
         *
         * On Windows prefer `sun.jnu.encoding` / the JVM default charset so GBK console
         * messages decode correctly. Elsewhere keep UTF-8.
         */
        fun platformOutputCharset(): Charset {
            val isWindows = System.getProperty("os.name").lowercase().contains("win")
            if (!isWindows) return StandardCharsets.UTF_8
            val native = System.getProperty("sun.jnu.encoding")?.trim().orEmpty()
            if (native.isNotEmpty()) {
                return runCatching { Charset.forName(native) }.getOrDefault(Charset.defaultCharset())
            }
            return Charset.defaultCharset()
        }
    }
}
