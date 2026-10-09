package com.agentgo.models.controller

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.models.exception.ModelErrorResponses
import com.agentgo.models.exception.ModelException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Translates [ModelException] failures into the shared HTTP response envelope.
 */
@RestControllerAdvice(basePackageClasses = [ModelController::class])
class ModelExceptionHandler {
    /**
     * Handles domain model failures raised by the models module.
     *
     * @param exception domain failure
     * @return HTTP response with the mapped business code
     */
    @ExceptionHandler(ModelException::class)
    fun handleModelException(exception: ModelException): ResponseEntity<CommonHttpResponse<Nothing>> =
        ResponseEntity.status(exception.status).body(ModelErrorResponses.of(exception))
}
