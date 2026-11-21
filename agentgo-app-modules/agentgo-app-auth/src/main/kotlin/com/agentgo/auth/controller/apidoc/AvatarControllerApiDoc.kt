package com.agentgo.auth.controller.apidoc

import com.agentgo.commons.api.ApiPaths
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.dto.file.FileMetadataResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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

/**
 * Springdoc contract for the authenticated user's avatar under `/api/v1/files/avatar`.
 *
 * The avatar is file storage, not an authentication operation. Login and profile stay at `/api/auth`.
 */
@Tag(
    name = "Files",
    description = "Versioned user-owned file bytes at /api/v1/files. " +
        "Workspace files are addressed by a relative path query such as reports/summary.md. " +
        "The path stays inside the caller's storage prefix, rejects absolute paths and '..', " +
        "and is unique per owner. PUT /workspace uploads or replaces one file and returns " +
        "2201 FILE-UPLOAD-COMPLETED. GET /workspace downloads the bytes. " +
        "GET /workspace/files lists metadata and returns 2200 FILE-LIST-RETRIEVED. " +
        "DELETE /workspace returns HTTP 204. PUT /avatar replaces the caller's avatar and " +
        "GET /avatar downloads it. Each upload is at most 100 MB. An empty file, an unreadable " +
        "upload, or an invalid path returns FILE-012, FILE-014, or FILE-013. A missing file " +
        "returns FILE-005. Missing authentication uses AUTH-002.",
)
@RequestMapping(ApiPaths.V1_FILES)
interface AvatarControllerApiDoc {
    @Operation(
        summary = "Replace the authenticated user's avatar",
        description = "Stores multipart field file as the caller's avatar and replaces any previous " +
            "avatar. Returns file metadata whose download URL is /api/v1/files/avatar. " +
            "Success code is 2201 FILE-UPLOAD-COMPLETED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Avatar updated (2201 FILE-UPLOAD-COMPLETED)", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Empty file (FILE-012) or unreadable upload (FILE-014)"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "413", description = "File exceeds the maximum size of 100 MB"),
        ],
    )
    @PutMapping("/avatar", consumes = ["multipart/form-data"])
    fun uploadAvatar(
        @Parameter(description = "Avatar image bytes. The multipart field name is file.", required = true)
        @RequestPart("file") file: MultipartFile,
    ): CommonHttpResponse<FileMetadataResponse>

    @Operation(
        summary = "Download the authenticated user's avatar",
        description = "Returns the caller's avatar bytes. The response is the file body, not the " +
            "CommonHttpResponse envelope. The browser must send the session cookie.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Avatar bytes"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "404", description = "Avatar not found (FILE-005)"),
        ],
    )
    @GetMapping("/avatar")
    fun downloadAvatar(): ResponseEntity<ByteArrayResource>
}
