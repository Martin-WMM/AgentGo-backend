package com.agentgo.events.aspect

import com.agentgo.events.annotation.RecordSystemEvent
import com.agentgo.events.service.SystemEventRecorder
import com.agentgo.events.support.SystemEventSpelEvaluator
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Intercepts methods marked with [RecordSystemEvent] and writes system events
 * through [SystemEventRecorder] after a successful return (and optionally after
 * a failure when [RecordSystemEvent.recordOnFailure] is true).
 */
@Aspect
@Component("recordSystemEventAspect")
class RecordSystemEventAspect(
    private val systemEventRecorder: SystemEventRecorder,
    private val systemEventSpelEvaluator: SystemEventSpelEvaluator,
) {
    private val logger = LoggerFactory.getLogger(RecordSystemEventAspect::class.java)

    /**
     * Proceeds with the join point, then records a system event when the protocol
     * condition matches.
     *
     * @param joinPoint annotated service method
     * @param recordSystemEvent annotation instance
     * @return the original method result
     */
    @Around("@annotation(recordSystemEvent)")
    fun recordEvent(
        joinPoint: ProceedingJoinPoint,
        recordSystemEvent: RecordSystemEvent,
    ): Any? {
        return try {
            val result = joinPoint.proceed()
            tryRecord(joinPoint, recordSystemEvent, result = result, error = null)
            result
        } catch (exception: Throwable) {
            if (recordSystemEvent.recordOnFailure) {
                tryRecord(joinPoint, recordSystemEvent, result = null, error = exception)
            }
            throw exception
        }
    }

    private fun tryRecord(
        joinPoint: ProceedingJoinPoint,
        annotation: RecordSystemEvent,
        result: Any?,
        error: Throwable?,
    ) {
        try {
            val evaluationContext = systemEventSpelEvaluator.context(joinPoint, result, error)
            if (!systemEventSpelEvaluator.matches(annotation.condition, evaluationContext)) {
                return
            }
            val name = systemEventSpelEvaluator.renderTemplate(annotation.name, evaluationContext)
            if (name.isNullOrBlank()) {
                logger.warn(
                    "Skipping system event for {}.{} because the rendered name is blank",
                    joinPoint.signature.declaringTypeName,
                    joinPoint.signature.name,
                )
                return
            }
            systemEventRecorder.record(
                name = name,
                domain = annotation.domain,
                comment = systemEventSpelEvaluator.renderTemplate(annotation.comment, evaluationContext),
                operator = systemEventSpelEvaluator.renderTemplate(annotation.operator, evaluationContext),
            )
        } catch (exception: Exception) {
            logger.warn(
                "Failed to evaluate @RecordSystemEvent on {}.{}: {}",
                joinPoint.signature.declaringTypeName,
                joinPoint.signature.name,
                exception.message,
            )
        }
    }
}
