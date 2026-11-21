package com.agentgo.file.controller

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.file.exception.FileErrorResponses
import com.agentgo.file.properties.FileProperties
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException

/**
 * Translates container multipart limit failures into the file error envelope.
 *
 * Tomcat raises this while resolving the upload, before the controller method runs,
 * so a controller-scoped advice cannot see it.
 */
@RestControllerAdvice
class UploadSizeExceptionHandler(
    private val properties: FileProperties,
) {
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSize(): ResponseEntity<CommonHttpResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(FileErrorResponses.tooLarge(properties.maxFileSizeBytes))
}
