package com.agentgo.auth.service

import com.agentgo.dto.file.FileMetadataResponse
import com.agentgo.file.storage.StoredFile
import org.springframework.web.multipart.MultipartFile

/**
 * Manages the authenticated user's avatar and its persisted file metadata.
 *
 * Upload stores the MinIO object first, then commits metadata in a database
 * transaction and compensates by deleting the object when metadata persistence
 * fails. Obsolete avatar objects are deleted only after a successful commit.
 */
interface AvatarService {
    /**
     * Stores or replaces the authenticated user's avatar.
     *
     * @param file uploaded multipart avatar content
     * @return persisted avatar metadata for API clients
     */
    fun uploadAvatar(file: MultipartFile): FileMetadataResponse

    /** Reads the authenticated user's avatar from MinIO. */
    fun downloadAvatar(): StoredFile
}
