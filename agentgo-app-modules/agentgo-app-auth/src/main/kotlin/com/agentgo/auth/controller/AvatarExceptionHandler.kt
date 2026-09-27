package com.agentgo.auth.controller

import com.agentgo.commons.http.HttpErrorResponse
import com.agentgo.file.exception.FileException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackageClasses = [AvatarController::class])
class AvatarExceptionHandler {
    @ExceptionHandler(FileException::class)
    fun handleFileException(exception: FileException): ResponseEntity<HttpErrorResponse> =
        ResponseEntity.status(exception.status).body(
            HttpErrorResponse(
                code = exception.status.value(),
                message = exception.message,
                retryable = exception.status.is5xxServerError,
            ),
        )

    @ExceptionHandler(IllegalStateException::class)
    fun handleStorageException(): ResponseEntity<HttpErrorResponse> =
        ResponseEntity.internalServerError().body(
            HttpErrorResponse(code = 500, message = "File storage is currently unavailable", retryable = true),
        )
}
