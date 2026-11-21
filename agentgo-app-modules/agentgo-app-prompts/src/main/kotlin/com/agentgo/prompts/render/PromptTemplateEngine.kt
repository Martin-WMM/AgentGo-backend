package com.agentgo.prompts.render

import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.prompts.exception.InvalidPromptTemplateRequestException
import org.springframework.stereotype.Component

/**
 * Selects the renderer for a [PromptEngine] and checks variable shape before rendering.
 *
 * @property renderers available template engines
 */
@Component
class PromptTemplateEngine(
    renderers: List<PromptTemplateRenderer>,
) {
    private val renderersByEngine = renderers.associateBy { it.engine }

    /**
     * Checks that [content] is valid for [engine].
     *
     * @param engine selected template engine
     * @param content template body
     * @throws InvalidPromptTemplateRequestException when the engine is unavailable or the body is invalid
     */
    fun validate(engine: PromptEngine, content: String) {
        renderer(engine).validate(content)
    }

    /**
     * Renders [content] with [variables] using [engine].
     *
     * @param engine selected template engine
     * @param content template body
     * @param variables caller-supplied variables
     * @return rendered text
     */
    fun render(engine: PromptEngine, content: String, variables: Map<String, Any?>): String {
        val checked = PromptVariableValidator.requireVariables(variables)
        return renderer(engine).render(content, checked)
    }

    private fun renderer(engine: PromptEngine): PromptTemplateRenderer =
        renderersByEngine[engine]
            ?: throw InvalidPromptTemplateRequestException("The prompt template engine is not available.")
}
