package com.agentgo.file.mapper

import com.agentgo.dto.file.FileMetadataResponse
import com.agentgo.file.entity.FileEntity

class FileMapperImpl : FileMapper {
    override fun toResponse(entity: FileEntity, downloadUrl: String): FileMetadataResponse = FileMetadataResponse(
        id = requireNotNull(entity.id),
        bucketName = entity.bucketName,
        objectKey = entity.objectKey,
        contentType = entity.contentType,
        sizeBytes = entity.sizeBytes,
        etag = entity.etag,
        versionId = entity.versionId,
        metadata = entity.metadata,
        createdAt = requireNotNull(entity.createdAt),
        updatedAt = requireNotNull(entity.updatedAt),
        downloadUrl = downloadUrl,
    )

    override fun toEntity(
        bucketName: String,
        objectKey: String,
        contentType: String,
        sizeBytes: Long,
        etag: String?,
        metadata: Map<String, String>,
    ): FileEntity = FileEntity(
        bucketName = bucketName,
        objectKey = objectKey,
        contentType = contentType,
        sizeBytes = sizeBytes,
        etag = etag,
        metadata = metadata,
    )
}
