package com.agentgo.auth.controller.apidoc

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.dto.file.FileMetadataResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Authentication", description = "Authenticated user identity and profile operations")
@RequestMapping("/api/files")
interface AvatarControllerApiDoc {
    @Operation(summary = "Update current user's avatar")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Avatar updated", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid file"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
        ],
    )
    @PutMapping("/avatar", consumes = ["multipart/form-data"])
    fun uploadAvatar(@RequestPart("file") file: MultipartFile): CommonHttpResponse<FileMetadataResponse>

    @Operation(summary = "Download current user's avatar")
    @ApiResponse(responseCode = "200", description = "Avatar content")
    @GetMapping("/avatar")
    fun downloadAvatar(): ResponseEntity<ByteArrayResource>
}
