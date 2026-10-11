package com.agentgo.prompts.service.impl

import com.agentgo.commons.dto.http.request.PageRequest
import com.agentgo.commons.dto.http.request.SortOrder
import com.agentgo.commons.dto.http.response.PageResponse
import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.dto.prompts.PromptTemplateCreateRequest
import com.agentgo.dto.prompts.PromptTemplatePreviewRequest
import com.agentgo.dto.prompts.PromptTemplateRenderRequest
import com.agentgo.dto.prompts.PromptTemplateRenderResponse
import com.agentgo.dto.prompts.PromptTemplateResponse
import com.agentgo.dto.prompts.PromptTemplateUpdateRequest
import com.agentgo.prompts.entity.PromptTemplateEntity
import com.agentgo.prompts.exception.DuplicatePromptTemplateNameException
import com.agentgo.prompts.exception.InvalidPromptTemplateRequestException
import com.agentgo.prompts.exception.PromptTemplateNotFoundException
import com.agentgo.prompts.mapper.PromptTemplateMapper
import com.agentgo.prompts.render.PromptTemplateEngine
import com.agentgo.prompts.render.PromptTemplateLimits
import com.agentgo.prompts.repository.PromptTemplateRepository
import com.agentgo.prompts.security.AuthenticatedUserIdResolver
import com.agentgo.prompts.service.PromptTemplateService
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest as SpringPageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * Default [PromptTemplateService] implementation backed by JPA persistence.
 *
 * Each public operation opens its own programmatic transaction. Rendering does not
 * keep a transaction open while the template engine runs.
 *
 * @property promptTemplateRepository template persistence access
 * @property promptTemplateMapper entity/DTO mapper
 * @property promptTemplateEngine template syntax checker and renderer
 * @property authenticatedUserIdResolver security-context owner resolver
 */
@Service("promptTemplateServiceImpl")
class PromptTemplateServiceImpl(
    private val promptTemplateRepository: PromptTemplateRepository,
    private val promptTemplateMapper: PromptTemplateMapper,
    private val promptTemplateEngine: PromptTemplateEngine,
    private val authenticatedUserIdResolver: AuthenticatedUserIdResolver,
    transactionManager: PlatformTransactionManager,
) : PromptTemplateService {
    private val writeTransaction = TransactionTemplate(transactionManager)
    private val readTransaction = TransactionTemplate(transactionManager).apply {
        isReadOnly = true
    }

    override fun create(request: PromptTemplateCreateRequest): PromptTemplateResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val normalized = normalizeCreate(request)
        return write {
            if (promptTemplateRepository.existsByOwnerUserIdAndName(ownerUserId, normalized.name)) {
                throw DuplicatePromptTemplateNameException()
            }
            val saved = promptTemplateRepository.save(promptTemplateMapper.toEntity(ownerUserId, normalized))
            promptTemplateRepository.flush()
            promptTemplateMapper.toResponse(saved)
        }
    }

    override fun get(id: UUID): PromptTemplateResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        return read {
            val entity = promptTemplateRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw PromptTemplateNotFoundException()
            promptTemplateMapper.toResponse(entity)
        }
    }

    override fun list(request: PageRequest): PageResponse<PromptTemplateResponse> {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val pageable = toPageable(request)
        return read {
            toPageResponse(promptTemplateRepository.findByOwnerUserId(ownerUserId, pageable), request)
        }
    }

    override fun update(id: UUID, request: PromptTemplateUpdateRequest): PromptTemplateResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val normalized = normalizeUpdate(request)
        return write {
            val entity = promptTemplateRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw PromptTemplateNotFoundException()
            if (promptTemplateRepository.existsByOwnerUserIdAndNameAndIdNot(ownerUserId, normalized.name, id)) {
                throw DuplicatePromptTemplateNameException()
            }
            promptTemplateMapper.applyUpdate(entity, normalized)
            val saved = promptTemplateRepository.save(entity)
            promptTemplateRepository.flush()
            promptTemplateMapper.toResponse(saved)
        }
    }

    override fun delete(id: UUID) {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        write {
            val entity = promptTemplateRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw PromptTemplateNotFoundException()
            promptTemplateRepository.delete(entity)
        }
    }

    override fun preview(request: PromptTemplatePreviewRequest): PromptTemplateRenderResponse {
        authenticatedUserIdResolver.requireUserId()
        val engine = requireEngine(request.engine)
        val content = requireContent(request.content)
        val variables = request.variables
        promptTemplateEngine.validate(engine, content)
        return PromptTemplateRenderResponse(
            promptTemplateEngine.render(engine, content, variables),
        )
    }

    override fun render(id: UUID, request: PromptTemplateRenderRequest): PromptTemplateRenderResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val stored = read {
            promptTemplateRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw PromptTemplateNotFoundException()
        }
        return PromptTemplateRenderResponse(
            promptTemplateEngine.render(stored.engine, stored.content, request.variables),
        )
    }

    private fun normalizeCreate(request: PromptTemplateCreateRequest): PromptTemplateCreateRequest {
        val engine = requireEngine(request.engine)
        val normalized = PromptTemplateCreateRequest().apply {
            name = requireName(request.name)
            this.engine = engine
            content = requireContent(request.content)
            description = normalizeDescription(request.description)
        }
        promptTemplateEngine.validate(engine, normalized.content)
        return normalized
    }

    private fun normalizeUpdate(request: PromptTemplateUpdateRequest): PromptTemplateUpdateRequest {
        val engine = requireEngine(request.engine)
        val normalized = PromptTemplateUpdateRequest().apply {
            name = requireName(request.name)
            this.engine = engine
            content = requireContent(request.content)
            description = normalizeDescription(request.description)
        }
        promptTemplateEngine.validate(engine, normalized.content)
        return normalized
    }

    private fun requireEngine(engine: PromptEngine?): PromptEngine =
        engine ?: throw InvalidPromptTemplateRequestException("The prompt template engine is required.")

    private fun requireName(value: String): String = requireText(value, "name", PromptTemplateLimits.NAME_MAX_LENGTH)

    private fun requireContent(value: String): String =
        requireText(value, "content", PromptTemplateLimits.CONTENT_MAX_LENGTH)

    private fun requireText(value: String, fieldName: String, maxLength: Int): String {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            throw InvalidPromptTemplateRequestException("The prompt template $fieldName must not be blank.")
        }
        if (trimmed.length > maxLength) {
            throw InvalidPromptTemplateRequestException(
                "The prompt template $fieldName must be at most $maxLength characters.",
            )
        }
        return trimmed
    }

    private fun normalizeDescription(description: String?): String? {
        val trimmed = description?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return null
        }
        if (trimmed.length > PromptTemplateLimits.DESCRIPTION_MAX_LENGTH) {
            throw InvalidPromptTemplateRequestException(
                "The prompt template description must be at most ${PromptTemplateLimits.DESCRIPTION_MAX_LENGTH} characters.",
            )
        }
        return trimmed
    }

    private fun toPageable(request: PageRequest): Pageable {
        if (request.size > MAX_PAGE_SIZE) {
            throw InvalidPromptTemplateRequestException(
                "The prompt template page size must be at most $MAX_PAGE_SIZE.",
            )
        }
        val requestedSort = request.sort.map { it.trim() }.filter { it.isNotEmpty() }
        val unknown = requestedSort.filterNot(ALLOWED_SORT_FIELDS::contains)
        if (unknown.isNotEmpty()) {
            throw InvalidPromptTemplateRequestException("The prompt template sort field is not supported.")
        }
        val sortFields = requestedSort.ifEmpty { listOf(DEFAULT_SORT_FIELD) }
        val direction = if (requestedSort.isEmpty() || request.order == SortOrder.DESC) {
            Sort.Direction.DESC
        } else {
            Sort.Direction.ASC
        }
        return SpringPageRequest.of(
            request.page - 1,
            request.size,
            Sort.by(direction, *sortFields.toTypedArray()),
        )
    }

    private fun toPageResponse(
        page: Page<PromptTemplateEntity>,
        request: PageRequest,
    ): PageResponse<PromptTemplateResponse> =
        PageResponse(
            data = page.content.map(promptTemplateMapper::toResponse),
            pageSize = request.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            empty = page.isEmpty,
            first = page.isFirst,
            last = page.isLast,
            size = page.numberOfElements,
            number = request.page,
        )

    private fun <T> read(action: () -> T): T =
        readTransaction.execute { action() } ?: error("Prompt template read transaction returned null")

    private fun <T> write(action: () -> T): T =
        writeTransaction.execute { action() } ?: error("Prompt template write transaction returned null")

    private companion object {
        const val MAX_PAGE_SIZE = 100
        const val DEFAULT_SORT_FIELD = "updatedAt"
        val ALLOWED_SORT_FIELDS = setOf("name", "engine", "createdAt", "updatedAt")
    }
}
