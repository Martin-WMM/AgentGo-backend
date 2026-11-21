package com.agentgo.file.exception

import org.springframework.http.HttpStatus

/** Raised when an upload is larger than the configured per-file limit. */
class FileTooLargeException(maxFileSizeBytes: Long) : FileException(
    code = 2011,
    responseType = "FILE-011",
    status = HttpStatus.PAYLOAD_TOO_LARGE,
    message = messageFor(maxFileSizeBytes),
) {
    companion object {
        /** Builds the client-facing message, including the configured limit. */
        fun messageFor(maxFileSizeBytes: Long): String =
            "File exceeds the maximum size of ${formatMegabytes(maxFileSizeBytes)}"

        private fun formatMegabytes(bytes: Long): String {
            val megabytes = bytes.toDouble() / (1024.0 * 1024.0)
            val label = if (megabytes % 1.0 == 0.0) megabytes.toLong().toString() else "%.1f".format(megabytes)
            return "$label MB"
        }
    }
}
