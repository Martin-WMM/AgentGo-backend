package com.agentgo.auth.service

import com.agentgo.dto.file.FileMetadataResponse
import com.agentgo.file.storage.StoredFile
import org.springframework.web.multipart.MultipartFile

/** Manages the authenticated user's avatar and its persisted file metadata. */
interface AvatarService {
    /** Stores or replaces the authenticated user's avatar. */
    fun uploadAvatar(file: MultipartFile): FileMetadataResponse

    /** Reads the authenticated user's avatar from MinIO. */
    fun downloadAvatar(): StoredFile
}
