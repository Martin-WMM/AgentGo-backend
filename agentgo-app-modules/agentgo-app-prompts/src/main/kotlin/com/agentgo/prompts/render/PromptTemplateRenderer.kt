package com.agentgo.prompts.render

import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.prompts.exception.InvalidPromptTemplateRequestException
import com.agentgo.prompts.exception.PromptTemplateRenderException

/**
 * Renders one [PromptEngine].
 */
interface PromptTemplateRenderer {
    /** Engine implemented by this renderer. */
    val engine: PromptEngine

    /**
     * Checks that [content] is structurally valid for [engine].
     *
     * Variable presence is not required. Callers validate variables separately at render time.
     *
     * @param content template body
     * @throws InvalidPromptTemplateRequestException when the template structure is invalid
     */
    fun validate(content: String)

    /**
     * Renders [content] with [variables].
     *
     * @param content template body that has already passed [validate]
     * @param variables JSON-compatible variable map
     * @return rendered text
     * @throws PromptTemplateRenderException when rendering fails
     * @throws InvalidPromptTemplateRequestException when a variable is incompatible with the engine
     */
    fun render(content: String, variables: Map<String, Any?>): String
}
