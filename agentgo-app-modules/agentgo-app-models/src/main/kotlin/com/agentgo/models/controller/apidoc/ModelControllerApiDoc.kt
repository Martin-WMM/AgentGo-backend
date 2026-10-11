package com.agentgo.models.controller.apidoc

import com.agentgo.commons.api.ApiPaths
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
@Tag(
    name = "Models",
    description = "Versioned user-owned AI model configurations at /api/v1/models. " +
        "Every model belongs to the authenticated user; operations never cross owners and " +
        "clients must not send a user id. name is unique per owner (HTTP 409, MODEL-003). " +
        "type is LLM, MULTIMODAL, EMBEDDING, ASR, TTS, SPEECH2SPEECH, or OTHER. " +
        "provider is an upstream identifier such as openai or ollama. configuration is a " +
        "provider-specific JSON object. description is optional and at most 2000 characters. " +
        "List accepts an optional type query and returns newest updates first. " +
        "Delete returns HTTP 204. Success codes are 8200 through 8204. " +
        "MODEL-001 is not found and MODEL-002 is an invalid request. Missing authentication uses AUTH-002.",
)
@RequestMapping(ApiPaths.V1_MODELS)
interface ModelControllerApiDoc {
    @Operation(
        summary = "Create a model for the authenticated user",
        description = "Stores a model configuration owned by the caller. Returns 8201 MODEL-CREATED. " +
            "A blank or oversized name or provider returns MODEL-002. A duplicate name returns MODEL-003.",
    )
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

    @Operation(
        summary = "Get one model owned by the authenticated user",
        description = "Returns one model when the id belongs to the caller. Another user's id is not found. " +
            "Returns 8202 MODEL-RETRIEVED.",
    )
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

    @Operation(
        summary = "List models owned by the authenticated user",
        description = "Lists the caller's models, newest update first. Omit type to return every category. " +
            "Returns 8200 MODEL-LIST-RETRIEVED.",
    )
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

    @Operation(
        summary = "Replace a model owned by the authenticated user",
        description = "Replaces name, provider, type, configuration, and description. Uniqueness ignores " +
            "the current id. Returns 8203 MODEL-UPDATED.",
    )
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

    @Operation(
        summary = "Delete a model owned by the authenticated user",
        description = "Deletes the caller's model and returns HTTP 204 with an empty body. " +
            "The success code 8204 MODEL-DELETED is registered but is not sent with this response.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Model deleted"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "404", description = "Model not found (MODEL-001)"),
        ],
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "Model identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: UUID,
    ): ResponseEntity<Void>
}
