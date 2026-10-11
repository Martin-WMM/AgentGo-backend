package com.agentgo.prompts.render

import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.prompts.exception.InvalidPromptTemplateRequestException
import com.agentgo.prompts.exception.PromptTemplateRenderException
import com.hubspot.jinjava.Jinjava
import com.hubspot.jinjava.JinjavaConfig
import com.hubspot.jinjava.interpret.TemplateError
import java.io.IOException
import org.springframework.stereotype.Component

/**
 * Renders Jinja2 templates with external loading disabled and a bounded output size.
 *
 * Include, import, from, and extends tags are rejected before interpretation. The resource
 * locator refuses every external load so a template cannot read files or other templates.
 */
@Component
class Jinja2PromptRenderer : PromptTemplateRenderer {
    override val engine: PromptEngine = PromptEngine.JINJA2

    private val jinjava = Jinjava().apply {
        resourceLocator = { _, _, _ ->
            throw IOException("Template resource loading is disabled.")
        }
    }

    private val validationConfig = configBuilder()
        .withValidationMode(true)
        .withFailOnUnknownTokens(false)
        .build()

    private val renderConfig = configBuilder()
        .withValidationMode(false)
        .withFailOnUnknownTokens(true)
        .build()

    override fun validate(content: String) {
        rejectForbiddenTags(content)
        try {
            val result = jinjava.renderForResult(content, emptyMap<String, Any?>(), validationConfig)
            if (result.errors.any { error -> error.reason == TemplateError.ErrorReason.SYNTAX_ERROR }) {
                throw InvalidPromptTemplateRequestException("The prompt template syntax is invalid.")
            }
        } catch (invalid: InvalidPromptTemplateRequestException) {
            throw invalid
        } catch (_: RuntimeException) {
            throw InvalidPromptTemplateRequestException("The prompt template syntax is invalid.")
        }
    }

    override fun render(content: String, variables: Map<String, Any?>): String {
        rejectForbiddenTags(content)
        try {
            val result = jinjava.renderForResult(content, variables, renderConfig)
            if (result.errors.any { error -> error.severity == TemplateError.ErrorType.FATAL }) {
                throw PromptTemplateRenderException()
            }
            val output = result.output.orEmpty()
            if (output.length > PromptTemplateLimits.OUTPUT_MAX_LENGTH) {
                throw PromptTemplateRenderException()
            }
            return output
        } catch (failure: PromptTemplateRenderException) {
            throw failure
        } catch (failure: InvalidPromptTemplateRequestException) {
            throw failure
        } catch (_: RuntimeException) {
            throw PromptTemplateRenderException()
        }
    }

    private fun rejectForbiddenTags(content: String) {
        if (FORBIDDEN_TAG.containsMatchIn(content)) {
            throw InvalidPromptTemplateRequestException(
                "The prompt template cannot load external templates.",
            )
        }
    }

    private fun configBuilder(): JinjavaConfig.Builder =
        JinjavaConfig.newBuilder()
            .withNestedInterpretationEnabled(false)
            .withEnableRecursiveMacroCalls(false)
            .withReadOnlyResolver(true)
            .withMaxRenderDepth(20)
            .withMaxOutputSize(PromptTemplateLimits.OUTPUT_MAX_LENGTH.toLong())
            .withRestrictedMethods(
                setOf("getClass", "forName", "wait", "notify", "notifyAll", "invoke"),
            )

    private companion object {
        val FORBIDDEN_TAG: Regex = Regex(
            """\{%-?\s*(include|import|from|extends)\b""",
            setOf(RegexOption.IGNORE_CASE),
        )
    }
}
