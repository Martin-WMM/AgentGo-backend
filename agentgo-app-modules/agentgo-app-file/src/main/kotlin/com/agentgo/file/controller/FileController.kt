package com.agentgo.file.controller

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.success.FileResponseCodeRegistry
import com.agentgo.dto.file.FileMetadataResponse
import com.agentgo.file.controller.apidoc.FileControllerApiDoc
import com.agentgo.file.service.WorkspaceFileService
import com.agentgo.file.storage.StoredFile
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class FileController(
    private val fileService: WorkspaceFileService,
) : FileControllerApiDoc {
    override fun uploadWorkspace(
        path: String,
        file: MultipartFile,
    ): CommonHttpResponse<FileMetadataResponse> =
        CommonHttpResponse(code = FileResponseCodeRegistry.UPLOAD_COMPLETED, responseType = FileResponseCodeRegistry.UPLOAD_COMPLETED_TYPE, message = "Workspace file uploaded", data = fileService.uploadWorkspace(path, file))

    override fun downloadWorkspace(path: String): ResponseEntity<ByteArrayResource> =
        toDownloadResponse(fileService.downloadWorkspace(path))

    override fun listWorkspace(): CommonHttpResponse<List<FileMetadataResponse>> =
        CommonHttpResponse(code = FileResponseCodeRegistry.LIST_RETRIEVED, responseType = FileResponseCodeRegistry.LIST_RETRIEVED_TYPE, message = "Workspace files listed", data = fileService.listWorkspace())

    override fun deleteWorkspace(path: String): ResponseEntity<Void> {
        fileService.deleteWorkspace(path)
        return ResponseEntity.noContent().build()
    }

    private fun toDownloadResponse(file: StoredFile): ResponseEntity<ByteArrayResource> = ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(file.contentType))
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename(file.filename).build().toString(),
        )
        .body(ByteArrayResource(file.bytes))
}
