package com.agentgo.auth.service.impl

import com.agentgo.auth.service.AvatarService
import com.agentgo.dto.file.FileMetadataResponse
import com.agentgo.file.properties.FileProperties
import com.agentgo.file.exception.EmptyFileException
import com.agentgo.file.exception.FileException
import com.agentgo.file.exception.FileTooLargeException
import com.agentgo.file.mapper.FileMapper
import com.agentgo.file.repository.FileRepository
import com.agentgo.file.service.FileService
import com.agentgo.file.service.bytes
import com.agentgo.file.storage.StoredFile
import java.security.MessageDigest
import java.util.Base64
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class AvatarServiceImpl(
    private val fileService: FileService,
    private val fileRepository: FileRepository,
    private val fileMapper: FileMapper,
    private val properties: FileProperties,
) : AvatarService {
    @Transactional
    override fun uploadAvatar(file: MultipartFile): FileMetadataResponse {
        val ownerId = currentOwnerId()
        validateFile(file)
        fileService.ensureBucket()
        val extension = extensionOf(file.originalFilename)
        val objectKey = buildString {
            append(properties.avatarPrefix.trimEnd('/'))
            append('/')
            append(ownerFolder(ownerId))
            if (extension.isNotEmpty()) append('.').append(extension)
        }
        val contentType = file.contentType?.takeIf { it.isNotBlank() } ?: "application/octet-stream"
        val originalFilename = safeFilename(file.originalFilename, "avatar")
        val metadata = mapOf("original-filename" to originalFilename)
        val etag = fileService.putObject(objectKey, file.bytes(), contentType, metadata)

        fileRepository.findByBucketNameAndObjectKeyStartingWith(
            properties.bucketName,
            avatarPrefix(ownerId),
        ).forEach {
            if (it.objectKey != objectKey) fileService.deleteObject(it.objectKey)
            fileRepository.delete(it)
        }
        val entity = fileRepository.save(
            fileMapper.toEntity(
                bucketName = properties.bucketName,
                objectKey = objectKey,
                contentType = contentType,
                sizeBytes = file.size,
                etag = etag,
                metadata = metadata,
            ),
        )
        return fileMapper.toResponse(entity, "/api/files/avatar")
    }

    @Transactional(readOnly = true)
    override fun downloadAvatar(): StoredFile {
        val ownerId = currentOwnerId()
        val entity = fileRepository.findByBucketNameAndObjectKeyStartingWith(
            properties.bucketName,
            avatarPrefix(ownerId),
        ).firstOrNull()
            ?: throw FileException(2005, "FILE-005", HttpStatus.NOT_FOUND, "Avatar was not found")
        return fileService.getObject(
            entity.objectKey,
            entity.contentType,
            entity.metadata["original-filename"] ?: entity.objectKey.substringAfterLast('/'),
        )
    }

    private fun currentOwnerId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated || authentication is AnonymousAuthenticationToken) {
            throw FileException(1002, "AUTH-002", HttpStatus.UNAUTHORIZED, "Authentication is required")
        }
        return authentication.name
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) throw EmptyFileException()
        if (file.size > properties.maxFileSizeBytes) throw FileTooLargeException(properties.maxFileSizeBytes)
    }

    private fun safeFilename(filename: String?, fallback: String): String =
        filename?.let { it.substringAfterLast('/').substringAfterLast('\\') }?.takeIf { it.isNotBlank() } ?: fallback

    private fun extensionOf(filename: String?): String =
        filename?.substringAfterLast('.', "")?.lowercase()?.takeIf { it.matches(Regex("[a-z0-9]{1,10}")) } ?: ""

    private fun ownerFolder(ownerId: String): String = Base64.getUrlEncoder().withoutPadding().encodeToString(
        MessageDigest.getInstance("SHA-256").digest(ownerId.toByteArray(Charsets.UTF_8)),
    )

    private fun avatarPrefix(ownerId: String): String = "${properties.avatarPrefix.trimEnd('/')}/${ownerFolder(ownerId)}/"
}
