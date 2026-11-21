package com.agentgo.models.exception

import org.springframework.http.HttpStatus

/** Raised when a model id is unknown for the authenticated owner. */
class ModelNotFoundException : ModelException(
    code = 8001,
    responseType = "MODEL-001",
    status = HttpStatus.NOT_FOUND,
    message = "The requested model was not found.",
)
