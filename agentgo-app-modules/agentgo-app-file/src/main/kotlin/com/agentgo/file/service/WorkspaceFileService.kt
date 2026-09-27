package com.agentgo.file.service

import com.agentgo.dto.file.FileMetadataResponse
import com.agentgo.file.storage.StoredFile
import org.springframework.web.multipart.MultipartFile

/** Coordinates authenticated workspace-file business operations and metadata persistence. */
interface WorkspaceFileService {
    /** Stores or replaces a file at a relative path in the current user's workspace. */
    fun uploadWorkspace(path: String, file: MultipartFile): FileMetadataResponse

    /** Reads a workspace file belonging to the current user. */
    fun downloadWorkspace(path: String): StoredFile

    /** Lists all workspace file metadata belonging to the current user. */
    fun listWorkspace(): List<FileMetadataResponse>

    /** Deletes a workspace file and its metadata for the current user. */
    fun deleteWorkspace(path: String)
}
