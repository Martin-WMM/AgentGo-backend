package com.agentgo.auth.service.impl

import com.agentgo.auth.support.NoOpPlatformTransactionManager
import com.agentgo.dto.file.FileMetadataResponse
import com.agentgo.file.entity.FileEntity
import com.agentgo.file.mapper.FileMapper
import com.agentgo.file.properties.FileProperties
import com.agentgo.file.repository.FileRepository
import com.agentgo.file.service.FileService
import com.agentgo.file.support.FileStorageTransactionSupport
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class AvatarServiceImplTest {
    private val fileService = mock<FileService>()
    private val fileRepository = mock<FileRepository>()
    private val fileMapper = mock<FileMapper>()
    private val properties = FileProperties(
        bucketName = "agentgo-bucket",
        avatarPrefix = "Avatars",
        maxFileSizeBytes = 1024,
    )
    private val storageTransactions = FileStorageTransactionSupport(NoOpPlatformTransactionManager(), fileService)
    private val service = AvatarServiceImpl(
        fileService,
        fileRepository,
        fileMapper,
        properties,
        storageTransactions,
    )

    @AfterTest
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `uploadAvatar stores object then persists metadata and deletes obsolete object after commit`() {
        authenticateAs("ada")
        val file = MockMultipartFile("file", "avatar.png", "image/png", byteArrayOf(1, 2, 3))
        val objectKey = avatarKey("ada", "png")
        val obsolete = FileEntity(
            id = UUID.randomUUID(),
            bucketName = properties.bucketName,
            objectKey = avatarKey("ada", "jpg"),
        )
        val entity = FileEntity(
            id = UUID.randomUUID(),
            bucketName = properties.bucketName,
            objectKey = objectKey,
            contentType = "image/png",
            sizeBytes = file.size,
            etag = "etag-1",
            metadata = mapOf("original-filename" to "avatar.png"),
            createdAt = Instant.parse("2026-10-11T00:00:00Z"),
            updatedAt = Instant.parse("2026-10-11T00:00:00Z"),
        )
        val response = FileMetadataResponse(
            id = entity.id!!,
            bucketName = entity.bucketName,
            objectKey = entity.objectKey,
            contentType = entity.contentType,
            sizeBytes = entity.sizeBytes,
            etag = entity.etag,
            versionId = null,
            metadata = entity.metadata,
            createdAt = entity.createdAt!!,
            updatedAt = entity.updatedAt!!,
            downloadUrl = "/api/v1/files/avatar",
        )
        whenever(fileRepository.findByBucketNameAndObjectKey(properties.bucketName, objectKey)).thenReturn(null)
        whenever(fileService.putObject(eq(objectKey), any(), eq("image/png"), any())).thenReturn("etag-1")
        whenever(
            fileRepository.findByBucketNameAndObjectKeyStartingWith(
                properties.bucketName,
                avatarPrefix("ada"),
            ),
        ).thenReturn(listOf(obsolete))
        whenever(
            fileMapper.toEntity(
                properties.bucketName,
                objectKey,
                "image/png",
                file.size,
                "etag-1",
                mapOf("original-filename" to "avatar.png"),
            ),
        ).thenReturn(entity)
        whenever(fileRepository.save(entity)).thenReturn(entity)
        whenever(fileMapper.toResponse(entity, "/api/v1/files/avatar")).thenReturn(response)

        assertEquals(response, service.uploadAvatar(file))
        verify(fileRepository).delete(obsolete)
        verify(fileRepository).save(entity)
        verify(fileService).deleteObject(obsolete.objectKey)
        verify(fileService, never()).deleteObject(objectKey)
    }

    @Test
    fun `uploadAvatar updates metadata when the same object key already exists`() {
        authenticateAs("ada")
        val file = MockMultipartFile("file", "avatar.png", "image/png", byteArrayOf(9, 8, 7))
        val objectKey = avatarKey("ada", "png")
        val existing = FileEntity(
            id = UUID.randomUUID(),
            bucketName = properties.bucketName,
            objectKey = objectKey,
            contentType = "image/jpeg",
            sizeBytes = 1,
            etag = "old-etag",
            metadata = mapOf("original-filename" to "old.png"),
            createdAt = Instant.parse("2026-10-11T00:00:00Z"),
            updatedAt = Instant.parse("2026-10-11T00:00:00Z"),
        )
        val response = FileMetadataResponse(
            id = existing.id!!,
            bucketName = existing.bucketName,
            objectKey = existing.objectKey,
            contentType = "image/png",
            sizeBytes = file.size,
            etag = "etag-2",
            versionId = null,
            metadata = mapOf("original-filename" to "avatar.png"),
            createdAt = existing.createdAt!!,
            updatedAt = Instant.parse("2026-10-11T01:00:00Z"),
            downloadUrl = "/api/v1/files/avatar",
        )
        whenever(fileRepository.findByBucketNameAndObjectKey(properties.bucketName, objectKey)).thenReturn(existing)
        whenever(fileService.putObject(eq(objectKey), any(), eq("image/png"), any())).thenReturn("etag-2")
        whenever(
            fileRepository.findByBucketNameAndObjectKeyStartingWith(
                properties.bucketName,
                avatarPrefix("ada"),
            ),
        ).thenReturn(listOf(existing))
        whenever(fileRepository.save(existing)).thenReturn(existing)
        whenever(fileMapper.toResponse(existing, "/api/v1/files/avatar")).thenReturn(response)

        assertEquals(response, service.uploadAvatar(file))
        assertEquals("image/png", existing.contentType)
        assertEquals(file.size, existing.sizeBytes)
        assertEquals("etag-2", existing.etag)
        assertEquals(mapOf("original-filename" to "avatar.png"), existing.metadata)
        verify(fileRepository, never()).delete(org.mockito.kotlin.any<FileEntity>())
        verify(fileMapper, never()).toEntity(any(), any(), any(), any(), any(), any())
        verify(fileService, never()).deleteObject(any())
    }

    @Test
    fun `uploadAvatar compensates stored object when metadata save fails`() {
        authenticateAs("ada")
        val file = MockMultipartFile("file", "avatar.png", "image/png", byteArrayOf(1, 2, 3))
        val objectKey = avatarKey("ada", "png")
        val entity = FileEntity(bucketName = properties.bucketName, objectKey = objectKey)
        whenever(fileRepository.findByBucketNameAndObjectKey(properties.bucketName, objectKey)).thenReturn(null)
        whenever(fileService.putObject(eq(objectKey), any(), eq("image/png"), any())).thenReturn("etag-1")
        whenever(
            fileRepository.findByBucketNameAndObjectKeyStartingWith(
                properties.bucketName,
                avatarPrefix("ada"),
            ),
        ).thenReturn(emptyList())
        whenever(fileMapper.toEntity(any(), any(), any(), any(), any(), any())).thenReturn(entity)
        whenever(fileRepository.save(entity)).thenThrow(IllegalStateException("db down"))

        assertFailsWith<IllegalStateException> {
            service.uploadAvatar(file)
        }
        verify(fileService).deleteObject(objectKey)
    }

    @Test
    fun `uploadAvatar does not compensate overwrite when metadata update fails`() {
        authenticateAs("ada")
        val file = MockMultipartFile("file", "avatar.png", "image/png", byteArrayOf(1, 2, 3))
        val objectKey = avatarKey("ada", "png")
        val existing = FileEntity(id = UUID.randomUUID(), bucketName = properties.bucketName, objectKey = objectKey)
        whenever(fileRepository.findByBucketNameAndObjectKey(properties.bucketName, objectKey)).thenReturn(existing)
        whenever(fileService.putObject(eq(objectKey), any(), eq("image/png"), any())).thenReturn("etag-1")
        whenever(
            fileRepository.findByBucketNameAndObjectKeyStartingWith(
                properties.bucketName,
                avatarPrefix("ada"),
            ),
        ).thenReturn(listOf(existing))
        whenever(fileRepository.save(existing)).thenThrow(IllegalStateException("db down"))

        assertFailsWith<IllegalStateException> {
            service.uploadAvatar(file)
        }
        verify(fileService, never()).deleteObject(objectKey)
    }

    @Test
    fun `downloadAvatar purges stale metadata when MinIO object is missing`() {
        authenticateAs("ada")
        val objectKey = avatarKey("ada", "png")
        val entity = FileEntity(
            id = UUID.randomUUID(),
            bucketName = properties.bucketName,
            objectKey = objectKey,
            contentType = "image/png",
            metadata = mapOf("original-filename" to "avatar.png"),
        )
        whenever(
            fileRepository.findByBucketNameAndObjectKeyStartingWith(
                properties.bucketName,
                avatarPrefix("ada"),
            ),
        ).thenReturn(listOf(entity))
        whenever(fileService.getObject(objectKey, "image/png", "avatar.png"))
            .thenThrow(IllegalStateException("Unable to read MinIO object"))
        whenever(fileRepository.findByBucketNameAndObjectKey(properties.bucketName, objectKey)).thenReturn(entity)

        val exception = assertFailsWith<com.agentgo.file.exception.FileException> {
            service.downloadAvatar()
        }
        assertEquals(404, exception.status.value())
        verify(fileRepository).delete(entity)
    }

    private fun authenticateAs(userId: String) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(userId, "n/a", emptyList())
    }

    private fun avatarKey(ownerId: String, extension: String): String =
        "${properties.avatarPrefix}/${ownerFolder(ownerId)}.$extension"

    private fun avatarPrefix(ownerId: String): String =
        "${properties.avatarPrefix}/${ownerFolder(ownerId)}"

    private fun ownerFolder(ownerId: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(
            MessageDigest.getInstance("SHA-256").digest(ownerId.toByteArray(StandardCharsets.UTF_8)),
        )
}
