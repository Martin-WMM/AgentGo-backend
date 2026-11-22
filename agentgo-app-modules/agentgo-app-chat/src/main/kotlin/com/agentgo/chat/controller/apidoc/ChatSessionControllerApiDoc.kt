package com.agentgo.chat.controller.apidoc

import com.agentgo.commons.api.ApiPaths
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.dto.chat.ChatMessageResponse
import com.agentgo.dto.chat.ChatSessionCreateRequest
import com.agentgo.dto.chat.ChatSessionResponse
import com.agentgo.dto.chat.ChatSessionUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Springdoc contract for authenticated chat-session endpoints.
 *
 * Ownership is resolved from the authentication context. Clients must not send a user id.
 */
@Tag(
    name = "Chat sessions",
    description = "Versioned user-owned chat sessions at /api/v1/chatSessions. " +
        "Each session owns a stable LangGraph threadId used by POST /api/v1/agent/stream. " +
        "Success codes are 10200 through 10205. CHAT-001 is not found and CHAT-002 is invalid. " +
        "Missing authentication uses AUTH-002.",
)
@RequestMapping(ApiPaths.V1_CHAT_SESSIONS)
interface ChatSessionControllerApiDoc {
    @Operation(
        summary = "Create a chat session for the authenticated user",
        description = "Creates a session with an optional title and a minted threadId. " +
            "Blank title becomes \"New conversation\". Returns 10201 CHAT-CREATED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Chat session created", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid chat request"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
        ],
    )
    @PostMapping
    fun create(@RequestBody request: ChatSessionCreateRequest): CommonHttpResponse<ChatSessionResponse>

    @Operation(
        summary = "Get one chat session owned by the authenticated user",
        description = "Returns one session when the id belongs to the caller. Returns 10202 CHAT-RETRIEVED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Chat session retrieved", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "404", description = "Chat session not found"),
        ],
    )
    @GetMapping("/{id}")
    fun get(
        @Parameter(description = "Chat session identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: Long,
    ): CommonHttpResponse<ChatSessionResponse>

    @Operation(
        summary = "List chat sessions owned by the authenticated user",
        description = "Lists the caller's sessions, newest update first. Returns 10200 CHAT-LIST-RETRIEVED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Chat sessions listed", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "Authentication required"),
        ],
    )
    @GetMapping
    fun list(): CommonHttpResponse<List<ChatSessionResponse>>

    @Operation(
        summary = "Rename a chat session owned by the authenticated user",
        description = "Updates the session title. Returns 10203 CHAT-UPDATED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Chat session updated", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid chat request"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "404", description = "Chat session not found"),
        ],
    )
    @PatchMapping("/{id}")
    fun update(
        @Parameter(description = "Chat session identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: Long,
        @RequestBody request: ChatSessionUpdateRequest,
    ): CommonHttpResponse<ChatSessionResponse>

    @Operation(
        summary = "Delete a chat session owned by the authenticated user",
        description = "Deletes the session and its messages. Returns HTTP 204.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Chat session deleted"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "404", description = "Chat session not found"),
        ],
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "Chat session identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: Long,
    ): ResponseEntity<Void>

    @Operation(
        summary = "List messages for a chat session owned by the authenticated user",
        description = "Returns chronological message history. Returns 10205 CHAT-MESSAGES-RETRIEVED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Chat messages listed", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "404", description = "Chat session not found"),
        ],
    )
    @GetMapping("/{id}/messages")
    fun listMessages(
        @Parameter(description = "Chat session identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: Long,
    ): CommonHttpResponse<List<ChatMessageResponse>>
}
