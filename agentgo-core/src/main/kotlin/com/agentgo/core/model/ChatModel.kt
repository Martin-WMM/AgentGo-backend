package com.agentgo.core.model

/**
 * Completes one chat request.
 *
 * Implementations perform the provider call. The agent graph decides how the
 * returned text is interpreted.
 */
fun interface ChatModel {
    /**
     * Returns the assistant completion for [request].
     *
     * @param request conversation and model selection
     * @return assistant text
     */
    fun complete(request: ChatRequest): ChatResponse

    /**
     * Streams assistant text deltas, then returns the full completion.
     *
     * The default implementation falls back to [complete] and does not emit
     * incremental deltas. Streaming providers override this method.
     *
     * @param request conversation and model selection
     * @param onDelta callback for each non-empty text fragment
     * @return full assistant text
     */
    fun stream(
        request: ChatRequest,
        onDelta: (String) -> Unit,
    ): ChatResponse = complete(request)
}
