package com.agentgo.file.controller.apidoc

import com.agentgo.commons.api.ApiPaths
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.dto.file.FileMetadataResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

/**
 * Springdoc contract for workspace file bytes under `/api/v1/files`.
 *
 * Ownership is resolved from the authentication context. Clients must not send a user id.
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
interface FileControllerApiDoc {
    @Operation(
        summary = "Upload or replace a workspace file",
        description = "Stores multipart field file at the relative path query. An existing file " +
            "at the same path is replaced. Returns metadata including a versioned download URL. " +
            "Success code is 2201 FILE-UPLOAD-COMPLETED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Workspace file uploaded (2201 FILE-UPLOAD-COMPLETED)", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid path (FILE-013), empty file (FILE-012), or unreadable upload (FILE-014)"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "413", description = "File exceeds the maximum size of 100 MB"),
        ],
    )
    @PutMapping("/workspace", consumes = ["multipart/form-data"])
    fun uploadWorkspace(
        @Parameter(
            description = "Relative path inside the current user's workspace. Example: reports/summary.md. " +
                "Absolute paths and '..' segments are rejected.",
            example = "reports/summary.md",
            `in` = ParameterIn.QUERY,
            required = true,
        )
        @RequestParam("path") path: String,
        @Parameter(description = "File bytes. The multipart field name is file.", required = true)
        @RequestPart("file") file: MultipartFile,
    ): CommonHttpResponse<FileMetadataResponse>

    @Operation(
        summary = "Download a workspace file",
        description = "Returns the stored bytes for the caller's relative path. The response is " +
            "the file body, not the CommonHttpResponse envelope.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Workspace file bytes"),
            ApiResponse(responseCode = "400", description = "Invalid workspace path (FILE-013)"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "404", description = "File not found (FILE-005)"),
        ],
    )
    @GetMapping("/workspace")
    fun downloadWorkspace(
        @Parameter(
            description = "Relative path of a file owned by the authenticated user",
            example = "reports/summary.md",
            `in` = ParameterIn.QUERY,
            required = true,
        )
        @RequestParam("path") path: String,
    ): ResponseEntity<ByteArrayResource>

    @Operation(
        summary = "List workspace files owned by the authenticated user",
        description = "Returns metadata for every file under the caller's storage prefix, " +
            "including object key, content type, size, and download URL. Success code is " +
            "2200 FILE-LIST-RETRIEVED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Workspace files listed (2200 FILE-LIST-RETRIEVED)", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
        ],
    )
    @GetMapping("/workspace/files")
    fun listWorkspace(): CommonHttpResponse<List<FileMetadataResponse>>

    @Operation(
        summary = "Delete a workspace file",
        description = "Deletes the caller's file at the relative path and returns HTTP 204 with an empty body.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Workspace file deleted"),
            ApiResponse(responseCode = "400", description = "Invalid workspace path (FILE-013)"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "404", description = "File not found (FILE-005)"),
        ],
    )
    @DeleteMapping("/workspace")
    fun deleteWorkspace(
        @Parameter(
            description = "Relative path of a file owned by the authenticated user",
            example = "reports/summary.md",
            `in` = ParameterIn.QUERY,
            required = true,
        )
        @RequestParam("path") path: String,
    ): ResponseEntity<Void>
}
