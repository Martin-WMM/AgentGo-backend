package com.agentgo.file.service.impl

import com.agentgo.file.properties.FileProperties
import com.agentgo.file.service.FileService
import com.agentgo.file.storage.StoredFile
import io.minio.BucketExistsArgs
import io.minio.GetObjectArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import java.io.ByteArrayInputStream
import org.springframework.stereotype.Service

/** Implements the MinIO-only file storage contract. */
@Service
class FileServiceImpl(
    private val minioClient: MinioClient,
    private val properties: FileProperties,
) : FileService {
    override fun ensureBucket() {
        try {
            val exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(properties.bucketName).build())
            if (!exists) minioClient.makeBucket(MakeBucketArgs.builder().bucket(properties.bucketName).build())
            createPrefixMarker("${properties.avatarPrefix.trimEnd('/')}/.keep")
            createPrefixMarker("${properties.workspacePrefix.trimEnd('/')}/.keep")
        } catch (exception: Exception) {
            throw IllegalStateException("Unable to initialize MinIO bucket", exception)
        }
    }

    override fun putObject(
        objectKey: String,
        bytes: ByteArray,
        contentType: String,
        metadata: Map<String, String>,
    ): String? = try {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(properties.bucketName)
                .`object`(objectKey)
                .stream(ByteArrayInputStream(bytes), bytes.size.toLong(), -1)
                .contentType(contentType)
                .userMetadata(metadata)
                .build(),
        ).etag()
    } catch (exception: Exception) {
        throw IllegalStateException("Unable to store MinIO object", exception)
    }

    override fun getObject(objectKey: String, contentType: String, filename: String): StoredFile = try {
        minioClient.getObject(GetObjectArgs.builder().bucket(properties.bucketName).`object`(objectKey).build()).use { input ->
            StoredFile(input.readAllBytes(), contentType, filename)
        }
    } catch (exception: Exception) {
        throw IllegalStateException("Unable to read MinIO object", exception)
    }

    override fun deleteObject(objectKey: String) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(properties.bucketName).`object`(objectKey).build())
        } catch (exception: Exception) {
            throw IllegalStateException("Unable to delete MinIO object", exception)
        }
    }

    private fun createPrefixMarker(objectKey: String) {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(properties.bucketName)
                .`object`(objectKey)
                .stream(ByteArrayInputStream(ByteArray(0)), 0, -1)
                .contentType("application/x-directory")
                .build(),
        )
    }
}
