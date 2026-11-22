package com.agentgo.core.model

/**
 * Assistant text returned by a model.
 *
 * @property content assistant text
 * @property modelId model that produced the text
 */
data class ChatResponse(
    val content: String,
    val modelId: String,
)
