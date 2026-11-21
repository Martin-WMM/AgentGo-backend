package com.agentgo.auth.controller

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.error.ErrorResponseCodeRegistry
import com.agentgo.file.exception.FileErrorResponses
import com.agentgo.file.exception.FileException
import com.agentgo.file.properties.FileProperties
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackageClasses = [AvatarController::class])
class AvatarExceptionHandler(
    private val properties: FileProperties,
) {
    @ExceptionHandler(FileException::class)
    fun handleFileException(exception: FileException): ResponseEntity<CommonHttpResponse<Nothing>> =
        ResponseEntity.status(exception.status).body(FileErrorResponses.of(exception))

    @ExceptionHandler(IllegalStateException::class)
    fun handleStorageException(exception: IllegalStateException): ResponseEntity<CommonHttpResponse<Nothing>> {
        if (FileErrorResponses.isUploadTooLarge(exception)) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(FileErrorResponses.tooLarge(properties.maxFileSizeBytes))
        }
        return ResponseEntity.internalServerError().body(
            CommonHttpResponse(
                code = 7001,
                message = "File storage is currently unavailable",
                retryable = true,
                success = false,
                responseType = "SYSTEM-001",
                error = ErrorResponseCodeRegistry.description("SYSTEM-001"),
            ),
        )
    }
}
