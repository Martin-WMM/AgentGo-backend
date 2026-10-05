package com.agentgo.file.controller.apidoc

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
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Files", description = "Workspace file operations")
@RequestMapping("/api/files")
interface FileControllerApiDoc {
    @Operation(summary = "Upload a workspace file")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Workspace file uploaded", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid workspace path or file"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "413", description = "File exceeds the maximum size of 100 MB"),
        ],
    )
    @PutMapping("/workspace", consumes = ["multipart/form-data"])
    fun uploadWorkspace(
        @Parameter(description = "Relative path inside the current user's workspace", example = "reports/summary.md", `in` = ParameterIn.QUERY)
        @RequestParam("path") path: String,
        @RequestPart("file") file: MultipartFile,
    ): CommonHttpResponse<FileMetadataResponse>

    @Operation(summary = "Download a workspace file")
    @GetMapping("/workspace")
    fun downloadWorkspace(@RequestParam("path") path: String): ResponseEntity<ByteArrayResource>

    @Operation(summary = "List current user's workspace files")
    @GetMapping("/workspace/files")
    fun listWorkspace(): CommonHttpResponse<List<FileMetadataResponse>>

    @Operation(summary = "Delete a workspace file")
    @ApiResponse(responseCode = "204", description = "Workspace file deleted")
    @DeleteMapping("/workspace")
    fun deleteWorkspace(@RequestParam("path") path: String): ResponseEntity<Void>
}
