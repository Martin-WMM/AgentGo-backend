package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for file-operation failure codes. */
object FileResponseCodeRegistry {
    val codeDescriptions = listOf(
        "FILE-001" to "File name or path is invalid." to "The requested file path is invalid.", "FILE-002" to "File exceeds an allowed size or type restriction." to "The uploaded file does not meet the allowed constraints.", "FILE-003" to "File storage operation failed." to "The file could not be stored.", "FILE-004" to "File metadata is in an invalid state." to "The file metadata cannot be processed.", "FILE-005" to "Requested file does not exist." to "The requested file was not found.", "FILE-006" to "File operation is not authorized." to "You do not have access to this file.", "FILE-007" to "File is unavailable due to its current state." to "The file is not available for this operation.", "FILE-008" to "File content or request parameters are invalid." to "The file request is invalid.", "FILE-009" to "File processing failed." to "The file could not be processed.", "FILE-010" to "Requested file operation is unsupported." to "This file operation is not supported.",
    ).associate { (pair, message) -> pair.first to ResponseCodeDescription(pair.second, "FILE", message) }
    val responseTypesByCode = (2001..2010).associateWith { "FILE-%03d".format(it - 2000) }
}
