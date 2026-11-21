package com.agentgo.commons.api

/**
 * Compile-time JSON examples for Springdoc `@ExampleObject` annotations.
 *
 * Keep values aligned with the real `CommonHttpResponse` failure/success shapes.
 */
object OpenApiExamples {
    const val AUTH_REQUIRED =
        "{\"code\":1002,\"message\":\"Authentication is required.\",\"data\":null," +
            "\"responseType\":\"AUTH-002\",\"retryable\":false,\"success\":false," +
            "\"timestamp\":\"2026-10-11T02:30:00Z\",\"requestId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\"," +
            "\"error\":{\"meaning\":\"Authentication credentials are missing.\"," +
            "\"category\":\"AUTHENTICATION\",\"exampleMessage\":\"Authentication is required.\"}}"

    const val FILE_INVALID_PATH =
        "{\"code\":2013,\"message\":\"Workspace path is invalid.\",\"data\":null," +
            "\"responseType\":\"FILE-013\",\"retryable\":false,\"success\":false," +
            "\"timestamp\":\"2026-10-11T02:30:00Z\",\"requestId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\"," +
            "\"error\":{\"meaning\":\"Workspace path is invalid.\"," +
            "\"category\":\"FILE\",\"exampleMessage\":\"Workspace path is invalid.\"}}"

    const val FILE_NOT_FOUND =
        "{\"code\":2005,\"message\":\"The requested file was not found.\",\"data\":null," +
            "\"responseType\":\"FILE-005\",\"retryable\":false,\"success\":false," +
            "\"timestamp\":\"2026-10-11T02:30:00Z\",\"requestId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\"," +
            "\"error\":{\"meaning\":\"Requested file does not exist.\"," +
            "\"category\":\"FILE\",\"exampleMessage\":\"The requested file was not found.\"}}"

    const val FILE_TOO_LARGE =
        "{\"code\":2011,\"message\":\"The uploaded file is larger than 100 MB.\",\"data\":null," +
            "\"responseType\":\"FILE-011\",\"retryable\":false,\"success\":false," +
            "\"timestamp\":\"2026-10-11T02:30:00Z\",\"requestId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\"," +
            "\"error\":{\"meaning\":\"Uploaded file exceeds the configured size limit.\"," +
            "\"category\":\"FILE\",\"exampleMessage\":\"The uploaded file is larger than 100 MB.\"}}"

    private const val FILE_PAYLOAD =
        "{\"id\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"bucketName\":\"agentgo-bucket\"," +
            "\"objectKey\":\"workspace/owner-hash/reports/summary.md\",\"contentType\":\"text/markdown\"," +
            "\"sizeBytes\":1024,\"etag\":\"d41d8cd98f00b204e9800998ecf8427e\",\"versionId\":null," +
            "\"metadata\":{},\"createdAt\":\"2026-10-11T02:30:00Z\",\"updatedAt\":\"2026-10-11T02:30:00Z\"," +
            "\"downloadUrl\":\"/api/v1/files/workspace?path=reports%2Fsummary.md\"}"

    const val FILE_UPLOADED =
        "{\"code\":2201,\"message\":\"Workspace file uploaded\",\"data\":$FILE_PAYLOAD," +
            "\"responseType\":\"FILE-UPLOAD-COMPLETED\",\"retryable\":false,\"success\":true," +
            "\"timestamp\":\"2026-10-11T02:30:00Z\",\"requestId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"error\":null}"

    const val FILE_LISTED =
        "{\"code\":2200,\"message\":\"Workspace files listed\",\"data\":[$FILE_PAYLOAD]," +
            "\"responseType\":\"FILE-LIST-RETRIEVED\",\"retryable\":false,\"success\":true," +
            "\"timestamp\":\"2026-10-11T02:30:00Z\",\"requestId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"error\":null}"

    private const val AVATAR_PAYLOAD =
        "{\"id\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"bucketName\":\"agentgo-bucket\"," +
            "\"objectKey\":\"avatar/owner-hash/avatar.png\",\"contentType\":\"image/png\"," +
            "\"sizeBytes\":2048,\"etag\":\"d41d8cd98f00b204e9800998ecf8427e\",\"versionId\":null," +
            "\"metadata\":{},\"createdAt\":\"2026-10-11T02:30:00Z\",\"updatedAt\":\"2026-10-11T02:30:00Z\"," +
            "\"downloadUrl\":\"/api/v1/files/avatar\"}"

    const val AVATAR_UPDATED =
        "{\"code\":1200,\"message\":\"Avatar updated\",\"data\":$AVATAR_PAYLOAD," +
            "\"responseType\":\"AUTH-AVATAR-UPDATED\",\"retryable\":false,\"success\":true," +
            "\"timestamp\":\"2026-10-11T02:30:00Z\",\"requestId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"error\":null}"

    const val AVATAR_NOT_FOUND =
        "{\"code\":2005,\"message\":\"Avatar was not found\",\"data\":null," +
            "\"responseType\":\"FILE-005\",\"retryable\":false,\"success\":false," +
            "\"timestamp\":\"2026-10-11T02:30:00Z\",\"requestId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\"," +
            "\"error\":{\"meaning\":\"Requested file does not exist.\"," +
            "\"category\":\"FILE\",\"exampleMessage\":\"The requested file was not found.\"}}"
}
