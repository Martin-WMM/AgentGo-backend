package com.agentgo.core.model

/**
 * Failure while calling the DashScope compatible-mode API.
 *
 * The message omits the API key and the raw response body.
 *
 * @param message safe description of the failure
 * @param cause upstream failure, when one exists
 */
class QwenChatException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
