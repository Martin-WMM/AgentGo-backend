package com.agentgo.core.model

/**
 * Chat model used when no Qwen credential is configured.
 *
 * The agent can still start. The first model call fails with a stable message.
 */
class UnavailableChatModel : ChatModel {
    override fun complete(request: ChatRequest): ChatResponse {
        throw IllegalStateException("Qwen API key is not configured.")
    }
}
