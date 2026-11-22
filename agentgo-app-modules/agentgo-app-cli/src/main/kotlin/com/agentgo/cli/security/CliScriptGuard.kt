package com.agentgo.cli.security

import com.agentgo.cli.exception.ForbiddenCliCommandException
import com.agentgo.cli.exception.InvalidCliScriptException
import com.agentgo.cli.properties.CliProperties
import org.springframework.stereotype.Component

/**
 * Validates CLI scripts before they reach the process runner.
 *
 * Enforces configurable allow/deny command basenames and optionally rejects shell
 * operators that enable chaining, redirection, or nested evaluation.
 *
 * @property properties CLI execution limits and command policy
 */
@Component
class CliScriptGuard(
    private val properties: CliProperties,
) {
    /**
     * Rejects scripts that violate the configured command policy.
     *
     * @param script raw script submitted by the caller
     * @throws InvalidCliScriptException when the script has no runnable command token
     * @throws ForbiddenCliCommandException when the command or operators are blocked
     */
    fun validate(script: String) {
        val normalized = script.trim()
        if (normalized.isEmpty()) {
            throw InvalidCliScriptException()
        }

        if (properties.denyShellOperators && containsForbiddenShellSyntax(normalized)) {
            throw ForbiddenCliCommandException(
                message = "The CLI script uses blocked shell operators.",
            )
        }

        val command = extractPrimaryCommand(normalized)
        if (command.isEmpty()) {
            throw InvalidCliScriptException()
        }

        val denied = properties.deniedCommands
            .asSequence()
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toSet()
        if (command in denied) {
            throw ForbiddenCliCommandException(
                message = "The CLI command \"$command\" is not allowed.",
            )
        }

        val allowed = properties.allowedCommands
            .asSequence()
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toSet()
        if (allowed.isNotEmpty() && command !in allowed) {
            throw ForbiddenCliCommandException(
                message = "The CLI command \"$command\" is not in the allowlist.",
            )
        }
    }

    companion object {
        private val FORBIDDEN_OPERATOR = Regex(
            """[;|`\n\r<>]|&&|\|\||\$\(|\$\{|(?<![A-Za-z0-9_])&(?![A-Za-z0-9_])""",
        )

        /**
         * Returns true when [script] contains shell chaining, redirection, or substitution syntax.
         *
         * @param script trimmed script body
         */
        fun containsForbiddenShellSyntax(script: String): Boolean =
            FORBIDDEN_OPERATOR.containsMatchIn(script)

        /**
         * Extracts the basename of the first command token (without Windows/Unix suffixes).
         *
         * @param script trimmed script body
         * @return lowercase command basename, or empty when no token exists
         */
        fun extractPrimaryCommand(script: String): String {
            val token = firstToken(script) ?: return ""
            val unquoted = unquote(token).trim()
            if (unquoted.isEmpty()) return ""
            val basename = unquoted
                .substringAfterLast('/')
                .substringAfterLast('\\')
            return stripExecutableSuffix(basename).lowercase()
        }

        private fun firstToken(script: String): String? {
            var index = 0
            while (index < script.length && script[index].isWhitespace()) {
                index++
            }
            if (index >= script.length) return null

            val quote = script[index]
            if (quote == '"' || quote == '\'') {
                var end = index + 1
                while (end < script.length && script[end] != quote) {
                    end++
                }
                val closing = if (end < script.length) end + 1 else end
                return script.substring(index, closing)
            }

            val start = index
            while (index < script.length && !script[index].isWhitespace()) {
                index++
            }
            return script.substring(start, index)
        }

        private fun unquote(token: String): String {
            if (token.length >= 2) {
                val first = token.first()
                val last = token.last()
                if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                    return token.substring(1, token.length - 1)
                }
            }
            return token
        }

        private fun stripExecutableSuffix(name: String): String {
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
