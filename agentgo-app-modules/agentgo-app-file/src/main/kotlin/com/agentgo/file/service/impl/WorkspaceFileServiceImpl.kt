package com.agentgo.file.service.impl

import com.agentgo.dto.file.FileMetadataResponse
import com.agentgo.file.properties.FileProperties
import com.agentgo.file.entity.FileEntity
import com.agentgo.file.exception.EmptyFileException
import com.agentgo.file.exception.FileException
import com.agentgo.file.exception.FileTooLargeException
import com.agentgo.file.exception.InvalidWorkspacePathException
import com.agentgo.file.mapper.FileMapper
import com.agentgo.file.repository.FileRepository
import com.agentgo.file.service.bytes
import com.agentgo.file.service.FileService
import com.agentgo.file.service.WorkspaceFileService
import com.agentgo.file.storage.StoredFile
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class WorkspaceFileServiceImpl(
    private val fileRepository: FileRepository,
    private val fileMapper: FileMapper,
    private val fileService: FileService,
    private val properties: FileProperties,
) : WorkspaceFileService {
    @Transactional
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
        return fileMapper.toResponse(
            fileRepository.save(entity),
            "/api/files/workspace?path=" + encode(normalizedPath),
        )
    }

    @Transactional(readOnly = true)
    override fun downloadWorkspace(path: String): StoredFile {
        val ownerId = currentOwnerId()
        val normalizedPath = normalizeWorkspacePath(path)
        val entity = findFile(workspaceKey(ownerId, normalizedPath))
        return fileService.getObject(entity.objectKey, entity.contentType, filenameOf(entity))
    }

    @Transactional(readOnly = true)
    override fun listWorkspace(): List<FileMetadataResponse> {
        val ownerId = currentOwnerId()
        return fileRepository.findByBucketNameAndObjectKeyStartingWith(properties.bucketName, workspacePrefix(ownerId))
            .map { fileMapper.toResponse(it, "/api/files/workspace?path=" + encode(pathFromWorkspaceKey(ownerId, it.objectKey))) }
    }

    @Transactional
    override fun deleteWorkspace(path: String) {
        val ownerId = currentOwnerId()
        val normalizedPath = normalizeWorkspacePath(path)
        val entity = findFile(workspaceKey(ownerId, normalizedPath))
        fileService.deleteObject(entity.objectKey)
        fileRepository.delete(entity)
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

    private fun contentTypeOf(file: MultipartFile): String = file.contentType?.takeIf { it.isNotBlank() } ?: "application/octet-stream"

    private fun safeFilename(filename: String?, fallback: String): String =
        filename?.let { it.substringAfterLast('/').substringAfterLast('\\') }?.takeIf { it.isNotBlank() } ?: fallback

    private fun normalizeWorkspacePath(path: String): String {
        val normalized = path.replace('\\', '/').trim('/')
        val parts = normalized.split('/')
        if (normalized.isBlank() || parts.any { it.isBlank() || it == "." || it == ".." }) throw InvalidWorkspacePathException()
        return parts.joinToString("/")
    }

    private fun workspaceKey(ownerId: String, path: String): String = "${properties.workspacePrefix.trimEnd('/')}/${ownerFolder(ownerId)}/$path"

    private fun workspacePrefix(ownerId: String): String = "${properties.workspacePrefix.trimEnd('/')}/${ownerFolder(ownerId)}/"

    private fun pathFromWorkspaceKey(ownerId: String, objectKey: String): String = objectKey.removePrefix("${properties.workspacePrefix.trimEnd('/')}/${ownerFolder(ownerId)}/")

    private fun ownerFolder(ownerId: String): String = Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(ownerId.toByteArray(StandardCharsets.UTF_8)))

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}
