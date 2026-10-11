package com.agentgo.file.service.impl

import com.agentgo.commons.api.ApiPaths
import com.agentgo.dto.file.FileMetadataResponse
import com.agentgo.file.entity.FileEntity
import com.agentgo.file.exception.EmptyFileException
import com.agentgo.file.exception.FileException
import com.agentgo.file.exception.FileTooLargeException
import com.agentgo.file.exception.InvalidWorkspacePathException
import com.agentgo.file.mapper.FileMapper
import com.agentgo.file.properties.FileProperties
import com.agentgo.file.repository.FileRepository
import com.agentgo.file.service.FileService
import com.agentgo.file.service.WorkspaceFileService
import com.agentgo.file.service.bytes
import com.agentgo.file.storage.StoredFile
import com.agentgo.file.support.FileStorageTransactionSupport
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

/**
 * Workspace file orchestration with programmatic DB transactions and MinIO compensation.
 *
 * @property fileRepository file metadata persistence
 * @property fileMapper entity and response mapping
 * @property fileService MinIO object storage
 * @property properties file storage configuration
 * @property storageTransactions JDBC and object-storage coordination
 */
@Service
class WorkspaceFileServiceImpl(
    private val fileRepository: FileRepository,
    private val fileMapper: FileMapper,
    private val fileService: FileService,
    private val properties: FileProperties,
    private val storageTransactions: FileStorageTransactionSupport,
) : WorkspaceFileService {
    override fun uploadWorkspace(path: String, file: MultipartFile): FileMetadataResponse {
        val ownerId = currentOwnerId()
        validateFile(file)
        val normalizedPath = normalizeWorkspacePath(path)
        fileService.ensureBucket()
        val objectKey = workspaceKey(ownerId, normalizedPath)
        val contentType = contentTypeOf(file)
        val originalFilename = safeFilename(file.originalFilename, normalizedPath.substringAfterLast('/'))
        val metadata = mapOf("original-filename" to originalFilename)
        val etag = fileService.putObject(objectKey, file.bytes(), contentType, metadata)
        return storageTransactions.commitMetadataForStoredObject(objectKey) {
            val existing = fileRepository.findByBucketNameAndObjectKey(properties.bucketName, objectKey)
            val entity = existing ?: fileMapper.toEntity(
                bucketName = properties.bucketName,
                objectKey = objectKey,
                contentType = contentType,
                sizeBytes = file.size,
                etag = etag,
                metadata = metadata,
            )
            if (existing != null) {
                entity.contentType = contentType
                entity.sizeBytes = file.size
                entity.etag = etag
                entity.metadata = metadata
            }
            fileMapper.toResponse(
                fileRepository.save(entity),
                ApiPaths.V1_FILES_WORKSPACE + "?path=" + encode(normalizedPath),
            )
        }
    }

    override fun downloadWorkspace(path: String): StoredFile =
        storageTransactions.readOnly {
            val ownerId = currentOwnerId()
            val normalizedPath = normalizeWorkspacePath(path)
            val entity = findFile(workspaceKey(ownerId, normalizedPath))
            fileService.getObject(entity.objectKey, entity.contentType, filenameOf(entity))
        }

    override fun listWorkspace(): List<FileMetadataResponse> =
        storageTransactions.readOnly {
            val ownerId = currentOwnerId()
            fileRepository.findByBucketNameAndObjectKeyStartingWith(properties.bucketName, workspacePrefix(ownerId))
                .map {
                    fileMapper.toResponse(
                        it,
                        ApiPaths.V1_FILES_WORKSPACE + "?path=" + encode(pathFromWorkspaceKey(ownerId, it.objectKey)),
                    )
                }
        }

    override fun deleteWorkspace(path: String) {
        val ownerId = currentOwnerId()
        val normalizedPath = normalizeWorkspacePath(path)
        storageTransactions.write {
            val entity = findFile(workspaceKey(ownerId, normalizedPath))
            fileRepository.delete(entity)
            storageTransactions.deleteObjectAfterCommit(entity.objectKey)
        }
    }

    override fun hasFilesInWorkspace(workspaceId: UUID): Boolean =
        storageTransactions.readOnly {
            val ownerId = currentOwnerId()
            val prefix = "${workspacePrefix(ownerId)}$workspaceId/"
            fileRepository.findByBucketNameAndObjectKeyStartingWith(properties.bucketName, prefix).isNotEmpty()
        }

    private fun currentOwnerId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated || authentication is AnonymousAuthenticationToken) {
            throw FileException(1002, "AUTH-002", HttpStatus.UNAUTHORIZED, "Authentication is required")
        }
        return authentication.name
    }

    private fun findFile(objectKey: String): FileEntity =
        fileRepository.findByBucketNameAndObjectKey(properties.bucketName, objectKey)
            ?: throw FileException(2005, "FILE-005", HttpStatus.NOT_FOUND, "File was not found")

    private fun filenameOf(entity: FileEntity): String =
        entity.metadata["original-filename"] ?: entity.objectKey.substringAfterLast('/')

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) throw EmptyFileException()
        if (file.size > properties.maxFileSizeBytes) throw FileTooLargeException(properties.maxFileSizeBytes)
    }

    private fun contentTypeOf(file: MultipartFile): String =
        file.contentType?.takeIf { it.isNotBlank() } ?: "application/octet-stream"

    private fun safeFilename(filename: String?, fallback: String): String =
        filename?.let { it.substringAfterLast('/').substringAfterLast('\\') }?.takeIf { it.isNotBlank() } ?: fallback

    private fun normalizeWorkspacePath(path: String): String {
        val normalized = path.replace('\\', '/').trim('/')
        val parts = normalized.split('/')
        if (normalized.isBlank() || parts.any { it.isBlank() || it == "." || it == ".." }) {
            throw InvalidWorkspacePathException()
        }
        return parts.joinToString("/")
    }

    private fun workspaceKey(ownerId: String, path: String): String =
        "${properties.workspacePrefix.trimEnd('/')}/${ownerFolder(ownerId)}/$path"

    private fun workspacePrefix(ownerId: String): String =
        "${properties.workspacePrefix.trimEnd('/')}/${ownerFolder(ownerId)}/"

    private fun pathFromWorkspaceKey(ownerId: String, objectKey: String): String =
        objectKey.removePrefix("${properties.workspacePrefix.trimEnd('/')}/${ownerFolder(ownerId)}/")

    private fun ownerFolder(ownerId: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(
            MessageDigest.getInstance("SHA-256").digest(ownerId.toByteArray(StandardCharsets.UTF_8)),
        )

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}
