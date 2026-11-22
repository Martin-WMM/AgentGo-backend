package com.agentgo.chat.controller

import com.agentgo.chat.controller.apidoc.ChatSessionControllerApiDoc
import com.agentgo.chat.service.ChatSessionService
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.success.ChatResponseCodeRegistry
import com.agentgo.dto.chat.ChatMessageResponse
import com.agentgo.dto.chat.ChatSessionCreateRequest
import com.agentgo.dto.chat.ChatSessionResponse
import com.agentgo.dto.chat.ChatSessionUpdateRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * HTTP adapter for authenticated chat-session CRUD operations.
 *
 * @property chatSessionService user-scoped chat application service
 */
@RestController
class ChatSessionController(
    private val chatSessionService: ChatSessionService,
) : ChatSessionControllerApiDoc {
    override fun create(request: ChatSessionCreateRequest): CommonHttpResponse<ChatSessionResponse> =
        CommonHttpResponse(
            code = ChatResponseCodeRegistry.CREATED,
            responseType = ChatResponseCodeRegistry.CREATED_TYPE,
            message = "Chat session created",
            data = chatSessionService.create(request),
        )

    override fun get(id: Long): CommonHttpResponse<ChatSessionResponse> =
        CommonHttpResponse(
            code = ChatResponseCodeRegistry.RETRIEVED,
            responseType = ChatResponseCodeRegistry.RETRIEVED_TYPE,
            message = "Chat session retrieved",
            data = chatSessionService.get(id),
        )

    override fun list(): CommonHttpResponse<List<ChatSessionResponse>> =
        CommonHttpResponse(
            code = ChatResponseCodeRegistry.LIST_RETRIEVED,
            responseType = ChatResponseCodeRegistry.LIST_RETRIEVED_TYPE,
            message = "Chat sessions listed",
            data = chatSessionService.list(),
        )

    override fun update(id: Long, request: ChatSessionUpdateRequest): CommonHttpResponse<ChatSessionResponse> =
        CommonHttpResponse(
            code = ChatResponseCodeRegistry.UPDATED,
            responseType = ChatResponseCodeRegistry.UPDATED_TYPE,
            message = "Chat session updated",
            data = chatSessionService.update(id, request),
        )

    override fun delete(id: Long): ResponseEntity<Void> {
        chatSessionService.delete(id)
        return ResponseEntity.noContent().build()
    }

    override fun listMessages(id: Long): CommonHttpResponse<List<ChatMessageResponse>> =
        CommonHttpResponse(
            code = ChatResponseCodeRegistry.MESSAGES_RETRIEVED,
            responseType = ChatResponseCodeRegistry.MESSAGES_RETRIEVED_TYPE,
            message = "Chat messages listed",
            data = chatSessionService.listMessages(id),
        )
}
