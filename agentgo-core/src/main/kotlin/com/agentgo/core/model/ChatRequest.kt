package com.agentgo.core.model

/**
 * One completion request.
 *
 * @property modelId upstream model identifier
 * @property messages conversation sent to the model
 * @property temperature sampling temperature
 */
data class ChatRequest(
    val modelId: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.2,
)
