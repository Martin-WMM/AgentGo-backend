package com.agentgo.file.controller

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.error.ErrorResponseCodeRegistry
import com.agentgo.file.exception.FileException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackageClasses = [FileController::class])
class FileExceptionHandler {
    @ExceptionHandler(FileException::class)
    fun handleFileException(exception: FileException): ResponseEntity<CommonHttpResponse<Nothing>> =
        ResponseEntity.status(exception.status).body(
            CommonHttpResponse(
                code = exception.code,
                message = exception.message,
                retryable = exception.status.is5xxServerError,
                success = false,
                responseType = exception.responseType,
                error = ErrorResponseCodeRegistry.description(exception.responseType),
            ),
        )

    @ExceptionHandler(IllegalStateException::class)
    fun handleStorageException(exception: IllegalStateException): ResponseEntity<CommonHttpResponse<Nothing>> =
        ResponseEntity.internalServerError().body(
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
