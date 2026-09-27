package com.agentgo.file.controller

import com.agentgo.commons.http.HttpResponse
import com.agentgo.dto.file.FileMetadataResponse
import com.agentgo.file.controller.apidoc.FileControllerApiDoc
import com.agentgo.file.service.WorkspaceFileService
import com.agentgo.file.storage.StoredFile
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
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
    ): HttpResponse<FileMetadataResponse> =
        HttpResponse(code = HttpStatus.OK.value(), message = "Workspace file uploaded", data = fileService.uploadWorkspace(path, file))

    override fun downloadWorkspace(path: String): ResponseEntity<ByteArrayResource> =
        toDownloadResponse(fileService.downloadWorkspace(path))

    override fun listWorkspace(): HttpResponse<List<FileMetadataResponse>> =
        HttpResponse(code = HttpStatus.OK.value(), message = "Workspace files listed", data = fileService.listWorkspace())

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
