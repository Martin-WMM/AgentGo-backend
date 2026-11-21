package com.agentgo.file.filter

import com.agentgo.file.exception.FileErrorResponses
import com.agentgo.file.properties.FileProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.nio.charset.StandardCharsets
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Writes a JSON file error when multipart parsing fails inside a downstream filter.
 *
 * Security filters can read the request body before Spring MVC resolves the upload.
 * In that path the size failure never reaches a controller advice.
 */
class UploadSizeExceptionFilter(
    private val properties: FileProperties,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (exception: Exception) {
            if (response.isCommitted || !FileErrorResponses.isUploadTooLarge(exception)) throw exception
            response.status = HttpStatus.PAYLOAD_TOO_LARGE.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = StandardCharsets.UTF_8.name()
            response.outputStream.write(
                FileErrorResponses.tooLargeJson(properties.maxFileSizeBytes).toByteArray(StandardCharsets.UTF_8),
            )
        }
    }
}
