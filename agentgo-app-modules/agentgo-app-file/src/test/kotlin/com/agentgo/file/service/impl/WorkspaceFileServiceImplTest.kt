package com.agentgo.file.service.impl

import com.agentgo.dto.file.FileMetadataResponse
import com.agentgo.file.entity.FileEntity
import com.agentgo.file.exception.EmptyFileException
import com.agentgo.file.exception.FileException
import com.agentgo.file.mapper.FileMapper
import com.agentgo.file.properties.FileProperties
import com.agentgo.file.repository.FileRepository
import com.agentgo.file.service.FileService
import com.agentgo.file.support.FileStorageTransactionSupport
import com.agentgo.file.support.NoOpPlatformTransactionManager
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

class WorkspaceFileServiceImplTest {
    private val fileRepository = mock<FileRepository>()
    private val fileMapper = mock<FileMapper>()
    private val fileService = mock<FileService>()
    private val properties = FileProperties(
        bucketName = "agentgo-bucket",
        workspacePrefix = "workspace",
        maxFileSizeBytes = 1024,
    )
    private val storageTransactions = FileStorageTransactionSupport(NoOpPlatformTransactionManager(), fileService)
    private val service = WorkspaceFileServiceImpl(
        fileRepository,
        fileMapper,
        fileService,
        properties,
        storageTransactions,
    )

    @AfterTest
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `uploadWorkspace stores object then persists metadata`() {
        authenticateAs("ada")
        val file = MockMultipartFile("file", "notes.txt", "text/plain", "hello".toByteArray())
        val objectKey = workspaceKey("ada", "docs/notes.txt")
        val entity = FileEntity(
            id = UUID.randomUUID(),
            bucketName = properties.bucketName,
            objectKey = objectKey,
            contentType = "text/plain",
            sizeBytes = file.size,
            etag = "etag-1",
            metadata = mapOf("original-filename" to "notes.txt"),
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
            downloadUrl = "/api/v1/files/workspace?path=docs%2Fnotes.txt",
        )
        whenever(fileService.putObject(eq(objectKey), any(), eq("text/plain"), any())).thenReturn("etag-1")
        whenever(fileRepository.findByBucketNameAndObjectKey(properties.bucketName, objectKey)).thenReturn(null)
        whenever(
            fileMapper.toEntity(
                properties.bucketName,
                objectKey,
                "text/plain",
                file.size,
                "etag-1",
                mapOf("original-filename" to "notes.txt"),
            ),
        ).thenReturn(entity)
        whenever(fileRepository.save(entity)).thenReturn(entity)
        whenever(fileMapper.toResponse(entity, "/api/v1/files/workspace?path=docs%2Fnotes.txt")).thenReturn(response)

        assertEquals(response, service.uploadWorkspace("docs/notes.txt", file))
        verify(fileService).ensureBucket()
        verify(fileService).putObject(eq(objectKey), any(), eq("text/plain"), any())
        verify(fileRepository).save(entity)
        verify(fileService, never()).deleteObject(objectKey)
    }

    @Test
    fun `uploadWorkspace compensates stored object when metadata save fails`() {
        authenticateAs("ada")
        val file = MockMultipartFile("file", "notes.txt", "text/plain", "hello".toByteArray())
        val objectKey = workspaceKey("ada", "docs/notes.txt")
        val entity = FileEntity(bucketName = properties.bucketName, objectKey = objectKey)
        whenever(fileService.putObject(eq(objectKey), any(), eq("text/plain"), any())).thenReturn("etag-1")
        whenever(fileRepository.findByBucketNameAndObjectKey(properties.bucketName, objectKey)).thenReturn(null)
        whenever(fileMapper.toEntity(any(), any(), any(), any(), any(), any())).thenReturn(entity)
        whenever(fileRepository.save(entity)).thenThrow(IllegalStateException("db down"))

        assertFailsWith<IllegalStateException> {
            service.uploadWorkspace("docs/notes.txt", file)
        }
        verify(fileService).deleteObject(objectKey)
    }

    @Test
    fun `deleteWorkspace removes metadata then deletes object after commit`() {
        authenticateAs("ada")
        val objectKey = workspaceKey("ada", "docs/notes.txt")
        val entity = FileEntity(
            id = UUID.randomUUID(),
            bucketName = properties.bucketName,
            objectKey = objectKey,
        )
        whenever(fileRepository.findByBucketNameAndObjectKey(properties.bucketName, objectKey)).thenReturn(entity)

        service.deleteWorkspace("docs/notes.txt")

        verify(fileRepository).delete(entity)
        verify(fileService).deleteObject(objectKey)
    }

    @Test
    fun `deleteWorkspace does not delete object when metadata lookup fails`() {
        authenticateAs("ada")
        val objectKey = workspaceKey("ada", "missing.txt")
        whenever(fileRepository.findByBucketNameAndObjectKey(properties.bucketName, objectKey)).thenReturn(null)

        val exception = assertFailsWith<FileException> {
            service.deleteWorkspace("missing.txt")
        }

        assertEquals("FILE-005", exception.responseType)
        verify(fileRepository, never()).delete(org.mockito.kotlin.any<FileEntity>())
        verify(fileService, never()).deleteObject(org.mockito.kotlin.any<String>())
    }

    @Test
    fun `uploadWorkspace rejects empty file before storage`() {
        authenticateAs("ada")
        val file = MockMultipartFile("file", "empty.txt", "text/plain", ByteArray(0))

        assertFailsWith<EmptyFileException> {
            service.uploadWorkspace("docs/empty.txt", file)
        }
        verify(fileService, never()).putObject(any(), any(), any(), any())
    }

    private fun authenticateAs(userId: String) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(userId, "n/a", emptyList())
    }

    private fun workspaceKey(ownerId: String, path: String): String =
        "${properties.workspacePrefix}/${ownerFolder(ownerId)}/$path"

    private fun ownerFolder(ownerId: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(
            MessageDigest.getInstance("SHA-256").digest(ownerId.toByteArray(StandardCharsets.UTF_8)),
        )
}
