package com.agentgo.dto.file

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

@Schema(description = "Metadata for an object stored in AgentGo MinIO storage")
data class FileMetadataResponse(
    @field:Schema(description = "File metadata identifier", format = "uuid")
    val id: UUID,
    @field:Schema(description = "MinIO bucket name", example = "agentgo-bucket")
    val bucketName: String,
    @field:Schema(description = "MinIO object key", example = "workspace/owner-hash/notes.md")
    val objectKey: String,
    @field:Schema(description = "MIME content type", example = "text/markdown")
    val contentType: String,
    @field:Schema(description = "File size in bytes", minimum = "0", example = "1024")
    val sizeBytes: Long,
    @field:Schema(description = "Object entity tag returned by MinIO", example = "d41d8cd98f00b204e9800998ecf8427e")
    val etag: String? = null,
    @field:Schema(description = "MinIO object version identifier")
    val versionId: String? = null,
    @field:Schema(description = "MinIO user metadata")
    val metadata: Map<String, String> = emptyMap(),
    @field:Schema(description = "Metadata creation timestamp", format = "date-time")
    val createdAt: Instant,
    @field:Schema(description = "Metadata last update timestamp", format = "date-time")
    val updatedAt: Instant,
    @field:Schema(description = "API URL used to access the object")
    val downloadUrl: String,
)
