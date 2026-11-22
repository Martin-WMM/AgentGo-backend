package com.agentgo.chat.config

import com.agentgo.chat.controller.ChatSessionController
import com.agentgo.chat.controller.ChatSessionExceptionHandler
import com.agentgo.chat.exception.ChatSessionNotFoundException
import com.agentgo.chat.exception.InvalidChatRequestException
import com.agentgo.chat.mapper.ChatMapperImpl
import com.agentgo.chat.repository.ChatMessageRepository
import com.agentgo.chat.repository.ChatSessionRepository
import com.agentgo.chat.security.AuthenticatedUserIdResolver
import com.agentgo.chat.service.ChatSessionTurnWriterImpl
import com.agentgo.chat.service.impl.ChatSessionServiceImpl
import com.agentgo.commons.dto.http.response.status.error.ChatResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.error.ExceptionResponseCodeRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Auto-configuration entry point for the user-owned chat session module.
 *
 * Registers persistence scanning, HTTP adapters, turn persistence, and stable
 * exception-to-response-code mappings used by the shared exception registry.
 */
@AutoConfiguration
@EntityScan("com.agentgo.chat.entity")
@EnableJpaRepositories(basePackageClasses = [ChatSessionRepository::class, ChatMessageRepository::class])
@Import(
    ChatSessionController::class,
    ChatSessionExceptionHandler::class,
    ChatMapperImpl::class,
    ChatSessionServiceImpl::class,
    ChatSessionTurnWriterImpl::class,
    AuthenticatedUserIdResolver::class,
)
class ChatAutoConfiguration {
    init {
        ExceptionResponseCodeRegistry.register<ChatSessionNotFoundException>(
            ChatResponseCodeRegistry.NOT_FOUND,
            ChatResponseCodeRegistry.NOT_FOUND_TYPE,
        )
        ExceptionResponseCodeRegistry.register<InvalidChatRequestException>(
            ChatResponseCodeRegistry.INVALID,
            ChatResponseCodeRegistry.INVALID_TYPE,
        )
    }
}
