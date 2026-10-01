package com.agentgo.file.mapper

import com.agentgo.dto.file.FileMetadataResponse
import com.agentgo.file.entity.FileEntity

interface FileMapper {
    fun toResponse(entity: FileEntity, downloadUrl: String): FileMetadataResponse

    fun toEntity(
        bucketName: String,
        objectKey: String,
        contentType: String,
        sizeBytes: Long,
        etag: String?,
        metadata: Map<String, String>,
    ): FileEntity
}
