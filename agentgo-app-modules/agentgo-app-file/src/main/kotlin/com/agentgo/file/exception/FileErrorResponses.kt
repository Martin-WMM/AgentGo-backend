package com.agentgo.file.exception

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.error.ErrorResponseCodeRegistry
import java.time.Instant
import java.util.UUID
import org.springframework.web.multipart.MaxUploadSizeExceededException

/** Maps file failures onto the shared HTTP response envelope. */
object FileErrorResponses {
    /** Converts a [FileException] into the response returned to API clients. */
    fun of(exception: FileException): CommonHttpResponse<Nothing> =
        CommonHttpResponse(
            code = exception.code,
            message = exception.message,
            retryable = exception.status.is5xxServerError,
            success = false,
            responseType = exception.responseType,
            error = ErrorResponseCodeRegistry.description(exception.responseType),
        )

    /** Response used when the container or the application rejects an oversized upload. */
    fun tooLarge(maxFileSizeBytes: Long): CommonHttpResponse<Nothing> = of(FileTooLargeException(maxFileSizeBytes))

    /**
     * JSON body for oversized uploads that fail before MVC can serialize a response.
     *
     * @param maxFileSizeBytes configured per-file limit
     * @return JSON matching [CommonHttpResponse]
     */
    fun tooLargeJson(maxFileSizeBytes: Long): String {
        val exception = FileTooLargeException(maxFileSizeBytes)
        val description = ErrorResponseCodeRegistry.description(exception.responseType)
        return buildString {
            append("{\"code\":")
            append(exception.code)
            append(",\"message\":")
            append(jsonString(exception.message))
            append(",\"data\":null,\"responseType\":")
            append(jsonString(exception.responseType))
            append(",\"retryable\":false,\"success\":false,\"timestamp\":")
            append(jsonString(Instant.now().toString()))
            append(",\"requestId\":")
            append(jsonString(UUID.randomUUID().toString()))
            append(",\"error\":{\"meaning\":")
            append(jsonString(description?.meaning.orEmpty()))
            append(",\"category\":")
            append(jsonString(description?.category.orEmpty()))
            append(",\"exampleMessage\":")
            append(jsonString(description?.exampleMessage.orEmpty()))
            append("}}")
        }
    }

    /** True when Tomcat or Spring rejected the upload before the file was stored. */
    fun isUploadTooLarge(throwable: Throwable?): Boolean {
        val seen = HashSet<Throwable>()
        var current = throwable
        while (current != null && seen.add(current)) {
            if (current is MaxUploadSizeExceededException) return true
            val name = current.javaClass.name
            if (name.endsWith("FileSizeLimitExceededException") || name.endsWith("SizeLimitExceededException")) return true
            current = current.cause
        }
        return false
    }

    private fun jsonString(value: String): String =
        buildString {
            append('"')
            value.forEach { character ->
                when (character) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(character)
                }
            }
            append('"')
        }
}
