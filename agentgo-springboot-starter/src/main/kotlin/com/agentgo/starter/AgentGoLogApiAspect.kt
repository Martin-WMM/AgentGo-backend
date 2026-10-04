package com.agentgo.starter

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.starter.annotation.AgentGoLogApi
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.UUID

/**
 * Logs the input and successful output of controller APIs marked with [AgentGoLogApi].
 *
 * @author Martin M. W.
 * @version 0.1.0-SNAPSHOT
 */
@Aspect
class AgentGoLogApiAspect {
    private val logger = LoggerFactory.getLogger(AgentGoLogApiAspect::class.java)

    @Around("@annotation(agentGoLogApi) && within(@org.springframework.web.bind.annotation.RestController *)")
    fun logApiInvocation(
        joinPoint: ProceedingJoinPoint,
        agentGoLogApi: AgentGoLogApi,
    ): Any? {
        val signature = joinPoint.signature as MethodSignature
        val request = currentRequest()
        val requestId = request?.getAttribute(REQUEST_ID_ATTRIBUTE) as? String
            ?: request?.getHeader(REQUEST_ID_HEADER)
            ?: UUID.randomUUID().toString()
        request?.setAttribute(REQUEST_ID_ATTRIBUTE, requestId)
        val name = agentGoLogApi.name.ifBlank { signature.method.name }
        val method = request?.method ?: "UNKNOWN"
        val url = request?.requestURI ?: "UNKNOWN"
        val startedAt = System.nanoTime()

        logger.info(
            "===> ({}) [{}] {}: Parameters = {}; Body = {}; headers = {}",
            requestId,
            method,
            url,
            redact(request?.parameterMap?.mapValues { it.value.toList() } ?: emptyMap<String, List<String>>()),
            redact(requestBody(signature, joinPoint.args)),
            redact(headers(request)),
        )

        return try {
            val result = joinPoint.proceed()
            logSuccess(requestId, method, url, startedAt, result)
            result
        } catch (exception: Throwable) {
            logger.warn("<=x= ({}) [{}] {} Exception Message = {}", requestId, method, url, exception.message)
            throw exception
        }
    }

    private fun logSuccess(
        requestId: String,
        method: String,
        url: String,
        startedAt: Long,
        result: Any?,
    ) {
        val responseEntity = result as? ResponseEntity<*>
        val response = responseEntity?.body ?: result
        val commonResponse = response as? CommonHttpResponse<*>
        val httpCode = responseEntity?.statusCode?.value() ?: currentResponseStatus()
        val responseCode = commonResponse?.code ?: "UNKNOWN"
        val responseType = commonResponse?.responseType ?: "UNKNOWN"
        val data = commonResponse?.data ?: response
        val elapsedMillis = (System.nanoTime() - startedAt) / NANOS_PER_MILLISECOND

        logger.info(
            "<=== ({}) [{}] {} - {} - {} - {} [{}ms] data = {}",
            requestId,
            method,
            url,
            httpCode,
            responseCode,
            responseType,
            elapsedMillis,
            redact(data),
        )
    }

    private fun requestBody(signature: MethodSignature, arguments: Array<Any?>): List<Any?> = signature.method.parameterAnnotations
        .mapIndexedNotNull { index, annotations ->
            arguments.getOrNull(index)?.takeIf { value -> annotations.any { it is RequestBody } }?.let(::redact)
        }

    private fun headers(request: HttpServletRequest?): Map<String, List<String>> = request?.headerNames
        ?.asIterator()
        ?.asSequence()
        ?.toList()
        ?.associateWith { headerName -> request.getHeaders(headerName).asIterator().asSequence().toList() }
        ?: emptyMap()

    private fun currentRequest(): HttpServletRequest? =
        (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request

    private fun currentResponseStatus(): Int =
        (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.response?.status ?: 200

    private fun redact(value: Any?): Any? = when (value) {
        null -> null
        is CharSequence -> if (value.length > 512) "${value.take(512)}..." else value.toString()
        is Array<*> -> value.map(::redact)
        is Iterable<*> -> value.map(::redact)
        is Map<*, *> -> value.mapValues { (key, mapValue) ->
            if (isSensitive(key?.toString())) "<redacted>" else redact(mapValue)
        }
        else -> if (isSensitive(value.toString())) {
            "<redacted>"
        } else {
            value
        }
    }

    private fun isSensitive(value: String?): Boolean = SENSITIVE_TERMS.any { term ->
        value?.contains(term, ignoreCase = true) == true
    }

    private companion object {
        const val REQUEST_ID_ATTRIBUTE = "agentgo.requestId"
        const val REQUEST_ID_HEADER = "X-Request-Id"
        const val NANOS_PER_MILLISECOND = 1_000_000L
        val SENSITIVE_TERMS = listOf("password", "secret", "token", "authorization")
    }
}
