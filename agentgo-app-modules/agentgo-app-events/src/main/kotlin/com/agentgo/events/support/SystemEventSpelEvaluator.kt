package com.agentgo.events.support

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.context.expression.MethodBasedEvaluationContext
import org.springframework.core.DefaultParameterNameDiscoverer
import org.springframework.expression.ExpressionParser
import org.springframework.expression.ParserContext
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.stereotype.Component

/**
 * Evaluates [com.agentgo.events.annotation.RecordSystemEvent] SpEL fragments.
 *
 * Name, comment, and operator fields are treated as templates: plain text is
 * kept as-is, and `#{…}` embeds expressions. [condition] is a raw SpEL boolean
 * expression (not a template).
 */
@Component("systemEventSpelEvaluator")
class SystemEventSpelEvaluator {
    private val parser: ExpressionParser = SpelExpressionParser()
    private val parameterNameDiscoverer = DefaultParameterNameDiscoverer()

    /**
     * Builds an evaluation context for one join-point invocation.
     *
     * @param joinPoint intercepted call
     * @param result successful return value, or null when recording a failure
     * @param error thrown exception when [com.agentgo.events.annotation.RecordSystemEvent.recordOnFailure]
     * is used, otherwise null
     */
    fun context(
        joinPoint: ProceedingJoinPoint,
        result: Any?,
        error: Throwable?,
    ): StandardEvaluationContext {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val root = MethodInvocationRoot(joinPoint.target, method.name, joinPoint.args)
        val evaluationContext = MethodBasedEvaluationContext(
            root,
            method,
            joinPoint.args,
            parameterNameDiscoverer,
        )
        evaluationContext.setVariable("result", result)
        evaluationContext.setVariable("error", error)
        joinPoint.args.forEachIndexed { index, argument ->
            evaluationContext.setVariable("p$index", argument)
        }
        return evaluationContext
    }

    /**
     * Renders a template string with optional `#{…}` SpEL placeholders.
     *
     * @param template annotation value; blank returns null
     * @param evaluationContext SpEL context
     * @return trimmed rendered text, or null when blank
     */
    fun renderTemplate(template: String, evaluationContext: StandardEvaluationContext): String? {
        if (template.isBlank()) return null
        val value = parser.parseExpression(template, TEMPLATE_CONTEXT)
            .getValue(evaluationContext, String::class.java)
        return value?.trim()?.takeIf { it.isNotEmpty() }
    }

    /**
     * Evaluates a raw SpEL boolean condition.
     *
     * @param expression blank means true
     * @param evaluationContext SpEL context
     * @return whether recording should proceed
     */
    fun matches(expression: String, evaluationContext: StandardEvaluationContext): Boolean {
        if (expression.isBlank()) return true
        return parser.parseExpression(expression).getValue(evaluationContext, Boolean::class.java) == true
    }

    /**
     * Root object exposed as `#root` for SpEL templates.
     *
     * @property target intercepted bean instance
     * @property methodName intercepted method name
     * @property args positional arguments
     */
    class MethodInvocationRoot(
        val target: Any?,
        val methodName: String,
        val args: Array<Any?>,
    )

    private companion object {
        val TEMPLATE_CONTEXT: ParserContext = object : ParserContext {
            override fun isTemplate(): Boolean = true
            override fun getExpressionPrefix(): String = "#{"
            override fun getExpressionSuffix(): String = "}"
        }
    }
}
