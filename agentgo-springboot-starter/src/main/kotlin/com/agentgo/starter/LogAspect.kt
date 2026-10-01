package com.agentgo.starter

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class LogAspect {
    private val logger = LoggerFactory.getLogger(LogAspect::class.java)

    @Around("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Service *)")
    fun logInvocation(joinPoint: ProceedingJoinPoint): Any? {
        val operation = joinPoint.signature.toShortString()
        logger.info("AgentGo request: operation={}, args={}", operation, redact(joinPoint.args))
        return try {
            val result = joinPoint.proceed()
            logger.info("AgentGo response: operation={}, result={}", operation, redact(result))
            result
        } catch (exception: Throwable) {
            logger.error("AgentGo failure: operation={}, type={}", operation, exception::class.simpleName, exception)
            throw exception
        }
    }

    private fun redact(value: Any?): Any? = when (value) {
        null -> null
        is CharSequence -> if (value.length > 512) "${value.take(512)}..." else value.toString()
        is Array<*> -> value.map(::redact)
        is Iterable<*> -> value.map(::redact)
        else -> if (value.toString().contains("password", ignoreCase = true) ||
            value.toString().contains("secret", ignoreCase = true) ||
            value.toString().contains("token", ignoreCase = true)
        ) "<redacted>" else value
    }
}
