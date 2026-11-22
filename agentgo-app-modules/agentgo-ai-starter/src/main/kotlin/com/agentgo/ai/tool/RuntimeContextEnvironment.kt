package com.agentgo.ai.tool

import com.agentgo.core.tool.ToolRuntimeContext

/**
 * Builds the process environment injected into Bash / CLI tool executions.
 *
 * Runtime context values always win over model-supplied extras so the model
 * cannot overwrite identity, locale, or token fields.
 */
object RuntimeContextEnvironment {
    const val RUN_ID: String = "AGENTGO_RUN_ID"
    const val THREAD_ID: String = "AGENTGO_THREAD_ID"
    const val USER_ID: String = "AGENTGO_USER_ID"
    const val SESSION_ID: String = "AGENTGO_SESSION_ID"
    const val SOURCE: String = "AGENTGO_SOURCE"
    const val LOCALE: String = "AGENTGO_LOCALE"
    const val TOKEN: String = "AGENTGO_TOKEN"
    const val USERNAME: String = "AGENTGO_USERNAME"
    const val LANG: String = "LANG"
    const val LC_ALL: String = "LC_ALL"

    /**
     * Merges optional model extras with [context]-derived AgentGo variables.
     *
     * @param context active run context
     * @param extras optional environment from tool arguments
     * @return environment map for [com.agentgo.dto.cli.CliExecutionRequest]
     */
    fun build(
        context: ToolRuntimeContext,
        extras: Map<String, String>? = null,
    ): Map<String, String> {
        val runtime = linkedMapOf<String, String>()
        put(runtime, RUN_ID, context.runId)
        put(runtime, THREAD_ID, context.threadId)

        var authorizationToken: String? = null
        context.headers.forEach { (rawKey, value) ->
            val key = rawKey.trim()
            if (key.isEmpty()) return@forEach
            runtime["AGENTGO_HEADER_${normalizeKey(key)}"] = value
            when {
                key.equals("Accept-Language", ignoreCase = true) -> {
                    put(runtime, LOCALE, value)
                    val posix = toPosixLocale(value)
                    put(runtime, LANG, posix)
                    put(runtime, LC_ALL, posix)
                }
                key.equals("Authorization", ignoreCase = true) -> {
                    authorizationToken = stripBearer(value)
                    put(runtime, TOKEN, authorizationToken.orEmpty())
                }
            }
        }

        context.metadata.forEach { (rawKey, value) ->
            val key = rawKey.trim()
            if (key.isEmpty()) return@forEach
            runtime["AGENTGO_META_${normalizeKey(key)}"] = value
            when (key) {
                "ownerUserId", "userId" -> put(runtime, USER_ID, value)
                "sessionId" -> put(runtime, SESSION_ID, value)
                "source" -> put(runtime, SOURCE, value)
                "username", "preferredUsername" -> put(runtime, USERNAME, value)
                "idToken", "accessToken", "token" -> {
                    if (authorizationToken.isNullOrBlank()) {
                        put(runtime, TOKEN, value)
                    }
                }
            }
        }

        val env = linkedMapOf<String, String>()
        extras.orEmpty().forEach { (key, value) ->
            val trimmed = key.trim()
            if (trimmed.isNotEmpty()) {
                env[trimmed] = value
            }
        }
        // Runtime context always wins over model-authored extras.
        env.putAll(runtime)
        return env
    }

    private fun put(env: MutableMap<String, String>, key: String, value: String) {
        val trimmed = value.trim()
        if (trimmed.isNotEmpty()) {
            env[key] = trimmed
        }
    }

    private fun normalizeKey(key: String): String =
        key.trim()
            .uppercase()
            .replace(Regex("[^A-Z0-9]+"), "_")
            .trim('_')

    private fun stripBearer(value: String): String {
        val trimmed = value.trim()
        return if (trimmed.startsWith("Bearer ", ignoreCase = true)) {
            trimmed.substring(7).trim()
        } else {
            trimmed
        }
    }

    private fun toPosixLocale(acceptLanguage: String): String {
        val primary = acceptLanguage.substringBefore(',').substringBefore(';').trim()
        if (primary.isEmpty()) return "en_US.UTF-8"
        val normalized = primary.replace('-', '_')
        return if (normalized.contains('.')) normalized else "$normalized.UTF-8"
    }
}
