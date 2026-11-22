package com.agentgo.core.model

/**
 * One message in a model conversation.
 *
 * @property role speaker of the message
 * @property content plain text content
 */
data class ChatMessage(
    val role: ChatRole,
    val content: String,
)
