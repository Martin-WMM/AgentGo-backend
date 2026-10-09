package com.agentgo.models.exception

import org.springframework.http.HttpStatus

/** Raised when the authenticated user already owns a model with the same name. */
class DuplicateModelNameException : ModelException(
    code = 8003,
    responseType = "MODEL-003",
    status = HttpStatus.CONFLICT,
    message = "A model with this name already exists.",
)
