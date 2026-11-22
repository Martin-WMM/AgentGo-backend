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
 * Runs one-shot CLI commands as discrete argv tokens.
 *
 * The first token must resolve through a fixed executable map (constant argv0 values) so the
 * process launcher never concatenates a user-controlled shell script string. Windows still
 * prefixes `cmd.exe /c` so PATH and console builtins resolve; stdout/stderr use the native
 * charset (typically GBK on Chinese Windows).
 *
 * Instances are owned by [com.agentgo.cli.pool.CliProcessWorker] slots in the startup pool.
 */
class DefaultCliProcessRunner(
    private val properties: CliProperties,
    private val processStarter: (ProcessBuilder) -> Process = { it.start() },
    private val outputCharset: Charset = platformOutputCharset(),
    private val fixedExecutables: Map<String, String> = DEFAULT_FIXED_EXECUTABLES,
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
        val tokens = tokenize(script.trim())
        if (tokens.isEmpty()) {
            throw CliFailedToStartException(message = "CLI script has no command.")
        }
        val commandName = stripExecutableSuffix(basename(unquote(tokens.first()))).lowercase()
        val executable = fixedExecutables[commandName]
            ?: throw CliFailedToStartException(
                message = "CLI executable \"$commandName\" is not permitted by the process runner.",
            )
        val arguments = tokens.drop(1).map { unquote(it) }
        val command = ArrayList<String>(2 + arguments.size)
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        if (isWindows) {
            command.add("cmd.exe")
            command.add("/c")
        }
        command.add(executable)
        command.addAll(arguments)
        return ProcessBuilder(command)
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
         * Constant argv0 values keyed by normalized command basenames.
         *
         * Map values are compile-time constants so process launch never uses a user-supplied
         * executable string. Policy still lives in [com.agentgo.cli.security.CliScriptGuard].
         */
        val DEFAULT_FIXED_EXECUTABLES: Map<String, String> = mapOf(
            "agentgo" to "agentgo",
            "echo" to "echo",
            "ping" to "ping",
            "printenv" to "printenv",
            "sleep" to "sleep",
        )

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

        internal fun tokenize(script: String): List<String> {
            val tokens = mutableListOf<String>()
            var index = 0
            while (index < script.length) {
                while (index < script.length && script[index].isWhitespace()) {
                    index++
                }
                if (index >= script.length) break
                val quote = script[index]
                if (quote == '"' || quote == '\'') {
                    var end = index + 1
                    while (end < script.length && script[end] != quote) {
                        end++
                    }
                    val closing = if (end < script.length) end + 1 else end
                    tokens.add(script.substring(index, closing))
                    index = closing
                } else {
                    val start = index
                    while (index < script.length && !script[index].isWhitespace()) {
                        index++
                    }
                    tokens.add(script.substring(start, index))
                }
            }
            return tokens
        }

        internal fun unquote(token: String): String {
            if (token.length >= 2) {
                val first = token.first()
                val last = token.last()
                if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                    return token.substring(1, token.length - 1)
                }
            }
            return token
        }

        internal fun basename(path: String): String =
            path.substringAfterLast('/').substringAfterLast('\\')

        internal fun stripExecutableSuffix(name: String): String {
            val lower = name.lowercase()
            for (suffix in listOf(".exe", ".cmd", ".bat", ".com", ".ps1")) {
                if (lower.endsWith(suffix)) {
                    return name.substring(0, name.length - suffix.length)
                }
            }
            return name
        }
    }
}
