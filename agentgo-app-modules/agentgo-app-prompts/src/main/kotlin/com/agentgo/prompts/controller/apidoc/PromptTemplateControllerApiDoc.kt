package com.agentgo.prompts.controller.apidoc

import com.agentgo.commons.api.ApiPaths
import com.agentgo.commons.dto.http.request.PageRequest
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.PageResponse
import com.agentgo.dto.prompts.PromptTemplateCreateRequest
import com.agentgo.dto.prompts.PromptTemplatePreviewRequest
import com.agentgo.dto.prompts.PromptTemplateRenderRequest
import com.agentgo.dto.prompts.PromptTemplateRenderResponse
import com.agentgo.dto.prompts.PromptTemplateResponse
import com.agentgo.dto.prompts.PromptTemplateUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Springdoc contract for authenticated prompt-template endpoints under `/api/v1/promptTemplates`.
 *
 * Ownership is resolved from the authentication context. Clients must not send a user id.
 */
@Tag(
    name = "Prompt templates",
    description = "Versioned user-owned prompt templates at /api/v1/promptTemplates. " +
        "Every template belongs to the authenticated user; operations never cross owners. " +
        "name is unique per owner (HTTP 409, PROMPT-003). engine is SIMPLE or JINJA2. SIMPLE replaces " +
        "{{ identifier }} placeholders only: the identifier matches [A-Za-z_][A-Za-z0-9_]*, whitespace " +
        "inside the braces is allowed, and expressions, filters, conditions, and loops are rejected. " +
        "JINJA2 renders a restricted Jinja2 template with conditions and loops; include, import, from, " +
        "and extends are rejected and external templates cannot be loaded. content is at most " +
        "100000 characters. description is optional and at most 2000 characters. " +
        "POST /preview renders an unsaved body. POST /{id}/render renders a stored template. " +
        "variables is a JSON object of scalars, arrays, and objects. Missing variables and " +
        "render failures return HTTP 400 and PROMPT-004 without an engine stack trace. " +
        "Delete returns HTTP 204. Success codes are 9200 through 9205. " +
        "PROMPT-001 is not found, PROMPT-002 is an invalid request, and PROMPT-005 is forbidden. " +
        "Missing authentication uses AUTH-002.",
)
@RequestMapping(ApiPaths.V1_PROMPT_TEMPLATES)
interface PromptTemplateControllerApiDoc {
    @Operation(
        summary = "Create a prompt template for the authenticated user",
        description = "Stores a template owned by the caller. The name must be unique for that user. " +
            "SIMPLE and JINJA2 bodies are syntax-checked before they are saved. " +
            "Returns 9201 PROMPT-CREATED. PROMPT-002 rejects a blank name, blank content, an " +
            "oversized field, or invalid template syntax. PROMPT-003 rejects a duplicate name.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Prompt template created (9201 PROMPT-CREATED)", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid prompt template request (PROMPT-002)"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "409", description = "Duplicate name for this owner (PROMPT-003)"),
        ],
    )
    @PostMapping
    fun create(@RequestBody request: PromptTemplateCreateRequest): CommonHttpResponse<PromptTemplateResponse>

    @Operation(
        summary = "Get one prompt template owned by the authenticated user",
        description = "Returns one template when the id belongs to the caller. Another user's id " +
            "is not found. Returns 9202 PROMPT-RETRIEVED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Prompt template retrieved (9202 PROMPT-RETRIEVED)", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "404", description = "Prompt template not found (PROMPT-001)"),
        ],
    )
    @GetMapping("/{id}")
    fun get(
        @Parameter(
            description = "Prompt template identifier owned by the authenticated user",
            `in` = ParameterIn.PATH,
            required = true,
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        )
        @PathVariable("id") id: UUID,
    ): CommonHttpResponse<PromptTemplateResponse>

    @Operation(
        summary = "List prompt templates owned by the authenticated user",
        description = "Returns one page of the caller's templates. page starts at 1. size defaults to 20 " +
            "and must be at most 100. sort accepts name, engine, createdAt, and updatedAt. " +
            "When sort is omitted, templates are ordered by updatedAt descending. " +
            "order is ASC or DESC and applies to every requested sort field. " +
            "Returns 9200 PROMPT-LIST-RETRIEVED. PROMPT-002 rejects an unknown sort field or a size above 100.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Prompt templates listed (9200 PROMPT-LIST-RETRIEVED)", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid page request (PROMPT-002)"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
        ],
    )
    @GetMapping
    fun list(
        @ParameterObject @ModelAttribute request: PageRequest,
    ): CommonHttpResponse<PageResponse<PromptTemplateResponse>>

    @Operation(
        summary = "Replace a prompt template owned by the authenticated user",
        description = "Replaces name, engine, content, and description. Uniqueness is checked " +
            "against other templates owned by the same user. The body is syntax-checked again. " +
            "Returns 9203 PROMPT-UPDATED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Prompt template updated (9203 PROMPT-UPDATED)", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid prompt template request (PROMPT-002)"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "404", description = "Prompt template not found (PROMPT-001)"),
            ApiResponse(responseCode = "409", description = "Duplicate name for this owner (PROMPT-003)"),
        ],
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(
            description = "Prompt template identifier owned by the authenticated user",
            `in` = ParameterIn.PATH,
            required = true,
        )
        @PathVariable("id") id: UUID,
        @RequestBody request: PromptTemplateUpdateRequest,
    ): CommonHttpResponse<PromptTemplateResponse>

    @Operation(
        summary = "Delete a prompt template owned by the authenticated user",
        description = "Deletes the caller's template and returns HTTP 204 with an empty body. " +
            "The success code 9204 PROMPT-DELETED is registered but is not sent with this response.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Prompt template deleted"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "404", description = "Prompt template not found (PROMPT-001)"),
        ],
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(
            description = "Prompt template identifier owned by the authenticated user",
            `in` = ParameterIn.PATH,
            required = true,
        )
        @PathVariable("id") id: UUID,
    ): ResponseEntity<Void>

    @Operation(
        summary = "Preview an unsaved prompt template",
        description = "Renders engine and content from the request without writing a row. " +
            "Use this while editing. SIMPLE substitutes string, number, and boolean values. " +
            "JINJA2 also accepts JSON arrays and objects. Returns 9205 PROMPT-RENDERED. " +
            "PROMPT-002 reports invalid syntax or illegal variables. PROMPT-004 reports a " +
            "missing variable or a render failure.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Prompt template rendered (9205 PROMPT-RENDERED)", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid template (PROMPT-002) or render failure (PROMPT-004)"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
        ],
    )
    @PostMapping("/preview")
    fun preview(@RequestBody request: PromptTemplatePreviewRequest): CommonHttpResponse<PromptTemplateRenderResponse>

    @Operation(
        summary = "Render a stored prompt template owned by the authenticated user",
        description = "Renders the saved engine and content with the supplied variables object. " +
            "The stored body is not changed. Returns 9205 PROMPT-RENDERED. An unknown id returns " +
            "PROMPT-001. A missing variable or render failure returns PROMPT-004.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Prompt template rendered (9205 PROMPT-RENDERED)", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid variables (PROMPT-002) or render failure (PROMPT-004)"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "404", description = "Prompt template not found (PROMPT-001)"),
        ],
    )
    @PostMapping("/{id}/render")
    fun render(
        @Parameter(
            description = "Prompt template identifier owned by the authenticated user",
            `in` = ParameterIn.PATH,
            required = true,
        )
        @PathVariable("id") id: UUID,
        @RequestBody request: PromptTemplateRenderRequest,
    ): CommonHttpResponse<PromptTemplateRenderResponse>
}
