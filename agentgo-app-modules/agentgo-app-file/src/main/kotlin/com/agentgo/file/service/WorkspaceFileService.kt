package com.agentgo.file.service

import com.agentgo.dto.file.FileMetadataResponse
import com.agentgo.file.storage.StoredFile
import java.util.UUID
import org.springframework.web.multipart.MultipartFile

/**
 * Coordinates authenticated workspace-file business operations and metadata persistence.
 *
 * Upload stores the MinIO object first, then commits metadata in a database transaction
 * and compensates by deleting the object when metadata persistence fails. Delete removes
 * metadata in a database transaction and deletes the MinIO object only after commit.
 */
interface WorkspaceFileService {
    /**
     * Stores or replaces a file at a relative path in the current user's workspace.
     *
     * @param path relative workspace path owned by the current user
     * @param file uploaded multipart content
     * @return persisted file metadata for API clients
     */
    fun uploadWorkspace(path: String, file: MultipartFile): FileMetadataResponse

    /** Reads a workspace file belonging to the current user. */
    fun downloadWorkspace(path: String): StoredFile

    /** Lists all workspace file metadata belonging to the current user. */
    fun listWorkspace(): List<FileMetadataResponse>

    /**
     * Deletes a workspace file and its metadata for the current user.
     *
     * Metadata is removed in a database transaction; the MinIO object is deleted after commit.
     */
    fun deleteWorkspace(path: String)

    /**
     * Returns whether the authenticated user still has objects under [workspaceId].
     *
     * @param workspaceId persistent workspace identifier used as the path prefix segment
     * @return `true` when at least one file metadata row exists under that prefix
     */
    fun hasFilesInWorkspace(workspaceId: UUID): Boolean
}
