package com.agentgo.auth.controller

import com.agentgo.auth.service.AvatarService
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.success.AuthResponseCodeRegistry
import com.agentgo.dto.file.FileMetadataResponse
import com.agentgo.auth.controller.apidoc.AvatarControllerApiDoc
import com.agentgo.file.storage.StoredFile
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class AvatarController(
    private val avatarService: AvatarService,
) : AvatarControllerApiDoc {
    override fun uploadAvatar(file: MultipartFile): CommonHttpResponse<FileMetadataResponse> =
        CommonHttpResponse(code = AuthResponseCodeRegistry.AVATAR_UPDATED, responseType = AuthResponseCodeRegistry.AVATAR_UPDATED_TYPE, message = "Avatar updated", data = avatarService.uploadAvatar(file))

    override fun downloadAvatar(): ResponseEntity<ByteArrayResource> = avatarService.downloadAvatar().toResponse()

    private fun StoredFile.toResponse(): ResponseEntity<ByteArrayResource> = ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
        .body(ByteArrayResource(bytes))
}
