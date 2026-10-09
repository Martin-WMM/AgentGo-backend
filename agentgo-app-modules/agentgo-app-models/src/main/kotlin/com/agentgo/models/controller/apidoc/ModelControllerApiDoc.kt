package com.agentgo.models.controller.apidoc

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.dto.models.ModelCreateRequest
import com.agentgo.dto.models.ModelResponse
import com.agentgo.dto.models.ModelType
import com.agentgo.dto.models.ModelUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Springdoc contract for authenticated model-management endpoints.
 *
 * Ownership is resolved from the authentication context. Clients must not send a user id.
 */
@Tag(name = "Models", description = "Authenticated user-owned AI model management")
@RequestMapping("/api/models")
interface ModelControllerApiDoc {
    @Operation(summary = "Create a model for the authenticated user")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Model created", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid model request"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "409", description = "Duplicate model name"),
        ],
    )
    @PostMapping
    fun create(@RequestBody request: ModelCreateRequest): CommonHttpResponse<ModelResponse>

    @Operation(summary = "Get one model owned by the authenticated user")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Model retrieved", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "404", description = "Model not found"),
        ],
    )
    @GetMapping("/{id}")
    fun get(
        @Parameter(description = "Model identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: UUID,
    ): CommonHttpResponse<ModelResponse>

    @Operation(summary = "List models owned by the authenticated user")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Models listed", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "Authentication required"),
        ],
    )
    @GetMapping
    fun list(
        @Parameter(description = "Optional model type filter", `in` = ParameterIn.QUERY, example = "LLM")
        @RequestParam(value = "type", required = false) type: ModelType?,
    ): CommonHttpResponse<List<ModelResponse>>

    @Operation(summary = "Update a model owned by the authenticated user")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Model updated", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid model request"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "404", description = "Model not found"),
            ApiResponse(responseCode = "409", description = "Duplicate model name"),
        ],
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "Model identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: UUID,
        @RequestBody request: ModelUpdateRequest,
    ): CommonHttpResponse<ModelResponse>

    @Operation(summary = "Delete a model owned by the authenticated user")
    @ApiResponse(responseCode = "204", description = "Model deleted")
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "Model identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: UUID,
    ): ResponseEntity<Void>
}
