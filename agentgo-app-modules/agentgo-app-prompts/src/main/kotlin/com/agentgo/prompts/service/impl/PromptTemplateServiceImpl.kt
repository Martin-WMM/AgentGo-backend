package com.agentgo.prompts.service.impl

import com.agentgo.commons.dto.http.request.BatchIds
import com.agentgo.commons.dto.http.request.PageRequest
import com.agentgo.commons.dto.http.request.SortOrder
import com.agentgo.commons.dto.http.response.PageResponse
import com.agentgo.dto.prompts.PromptTemplateCreateRequest
import com.agentgo.dto.prompts.PromptTemplatePreviewRequest
import com.agentgo.dto.prompts.PromptTemplateRenderRequest
import com.agentgo.dto.prompts.PromptTemplateRenderResponse
import com.agentgo.dto.prompts.PromptTemplateResponse
import com.agentgo.dto.prompts.PromptTemplateRevisionResponse
import com.agentgo.dto.prompts.PromptTemplateUpdateRequest
import com.agentgo.dto.prompts.PromptTemplateVersions
import com.agentgo.events.annotation.RecordSystemEvent
import com.agentgo.events.domain.SystemEventDomains
import com.agentgo.prompts.entity.PromptTemplateEntity
import com.agentgo.prompts.entity.PromptTemplateRevisionEntity
import com.agentgo.prompts.exception.DuplicatePromptTemplateNameException
import com.agentgo.prompts.exception.InvalidPromptTemplateRequestException
import com.agentgo.prompts.exception.PromptTemplateNotFoundException
import com.agentgo.prompts.mapper.PromptTemplateMapper
import com.agentgo.prompts.render.PromptTemplateEngine
import com.agentgo.prompts.render.PromptTemplateLimits
import com.agentgo.prompts.repository.PromptTemplateRepository
import com.agentgo.prompts.repository.PromptTemplateRevisionRepository
import com.agentgo.prompts.revision.PromptSnapshot
import com.agentgo.prompts.revision.PromptTemplateCommitDiff
import com.agentgo.prompts.revision.PromptTemplateRevisionMapper
import com.agentgo.prompts.security.AuthenticatedUserIdResolver
import com.agentgo.prompts.service.PromptTemplateService
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
 * keep a transaction open while the template engine runs. Field shape is checked by
 * Jakarta Bean Validation on the request DTO before this service runs. This class
 * keeps ownership, template syntax, uniqueness, and revision rules.
 *
 * @property promptTemplateRepository template persistence access
 * @property promptTemplateRevisionRepository commit history persistence access
 * @property promptTemplateMapper entity/DTO mapper
 * @property promptTemplateEngine template syntax checker and renderer
 * @property authenticatedUserIdResolver security-context owner resolver
 */
@Service("promptTemplateServiceImpl")
class PromptTemplateServiceImpl(
    private val promptTemplateRepository: PromptTemplateRepository,
    private val promptTemplateRevisionRepository: PromptTemplateRevisionRepository,
    private val promptTemplateMapper: PromptTemplateMapper,
    private val promptTemplateEngine: PromptTemplateEngine,
    private val authenticatedUserIdResolver: AuthenticatedUserIdResolver,
    transactionManager: PlatformTransactionManager,
) : PromptTemplateService {
    private val writeTransaction = TransactionTemplate(transactionManager)
    private val readTransaction = TransactionTemplate(transactionManager).apply {
        isReadOnly = true
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.PROMPTS,
        name = "Prompt template created",
        comment = "Created template #{#result.groupName}/#{#result.name} (#{#result.id})",
    )
    override fun create(request: PromptTemplateCreateRequest): PromptTemplateResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val engine = checkNotNull(request.engine) { "Prompt template engine is required." }
        promptTemplateEngine.validate(engine, request.content.trim())
        return write {
            if (
                promptTemplateRepository.existsByGroupNameAndNameAndCreator(
                    request.groupName.trim(),
                    request.name.trim(),
                    ownerUserId,
                )
            ) {
                throw DuplicatePromptTemplateNameException()
            }
            val saved = promptTemplateRepository.save(promptTemplateMapper.toEntity(ownerUserId, request))
            val snapshot = snapshotOf(saved)
            promptTemplateRevisionRepository.save(
                revision(
                    templateId = requireNotNull(saved.id) { "Persisted prompt template must have an identifier." },
                    revisionNumber = 1,
                    parentRevision = null,
                    message = commitMessage(request.message, defaultWhenBlank = true),
                    author = ownerUserId,
                    before = null,
                    after = snapshot,
                ),
            )
            promptTemplateRepository.flush()
            promptTemplateMapper.toResponse(saved)
        }
    }

    override fun get(id: Long): PromptTemplateResponse {
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

    @RecordSystemEvent(
        domain = SystemEventDomains.PROMPTS,
        name = "Prompt template updated",
        comment = "Updated template #{#result.groupName}/#{#result.name} (#{#id})",
    )
    override fun update(id: Long, request: PromptTemplateUpdateRequest): PromptTemplateResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val engine = checkNotNull(request.engine) { "Prompt template engine is required." }
        promptTemplateEngine.validate(engine, request.content.trim())
        return write {
            val entity = promptTemplateRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw PromptTemplateNotFoundException()
            val creator = entity.creator?.takeIf { it.isNotBlank() } ?: ownerUserId
            if (
                promptTemplateRepository.existsByGroupNameAndNameAndCreatorAndIdNot(
                    request.groupName.trim(),
                    request.name.trim(),
                    creator,
                    id,
                )
            ) {
                throw DuplicatePromptTemplateNameException()
            }
            val before = snapshotOf(entity)
            promptTemplateMapper.applyUpdate(entity, request)
            val after = snapshotOf(entity)
            if (before.sameAs(after)) {
                throw InvalidPromptTemplateRequestException("The prompt template has no changes to commit.")
            }
            val nextRevision = entity.headRevision + 1
            entity.headRevision = nextRevision
            entity.version = PromptTemplateVersions.LATEST
            val saved = promptTemplateRepository.save(entity)
            promptTemplateRevisionRepository.save(
                revision(
                    templateId = requireNotNull(saved.id),
                    revisionNumber = nextRevision,
                    parentRevision = nextRevision - 1,
                    message = commitMessage(request.message, defaultWhenBlank = false),
                    author = ownerUserId,
                    before = before,
                    after = after,
                ),
            )
            promptTemplateRepository.flush()
            promptTemplateMapper.toResponse(saved)
        }
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.PROMPTS,
        name = "Prompt template deleted",
        comment = "Deleted template #{#id}",
    )
    override fun delete(id: Long) {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        write {
            val entity = promptTemplateRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw PromptTemplateNotFoundException()
            promptTemplateRevisionRepository.deleteByTemplateId(requireNotNull(entity.id))
            promptTemplateRepository.delete(entity)
        }
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.PROMPTS,
        name = "Prompt templates deleted",
        comment = "Deleted #{#ids.size()} prompt templates",
    )
    override fun deleteAll(ids: List<Long>) {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val distinct = BatchIds.requireDistinctOr(ids) { InvalidPromptTemplateRequestException(it) }
        write {
            val entities = distinct.map { id ->
                promptTemplateRepository.findByIdAndOwnerUserId(id, ownerUserId)
                    ?: throw PromptTemplateNotFoundException()
            }
            entities.forEach { entity ->
                promptTemplateRevisionRepository.deleteByTemplateId(requireNotNull(entity.id))
                promptTemplateRepository.delete(entity)
            }
        }
    }

    override fun preview(request: PromptTemplatePreviewRequest): PromptTemplateRenderResponse {
        authenticatedUserIdResolver.requireUserId()
        val engine = checkNotNull(request.engine) { "Prompt template engine is required." }
        val content = request.content.trim()
        promptTemplateEngine.validate(engine, content)
        return PromptTemplateRenderResponse(
            promptTemplateEngine.render(engine, content, request.variables),
        )
    }

    override fun render(id: Long, request: PromptTemplateRenderRequest): PromptTemplateRenderResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val stored = read {
            promptTemplateRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw PromptTemplateNotFoundException()
        }
        return PromptTemplateRenderResponse(
            promptTemplateEngine.render(stored.engine, stored.content, request.variables),
        )
    }

    override fun listRevisions(id: Long, request: PageRequest): PageResponse<PromptTemplateRevisionResponse> {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val pageable = revisionPageable(request)
        return read {
            val entity = promptTemplateRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw PromptTemplateNotFoundException()
            val page = promptTemplateRevisionRepository.findByTemplateId(requireNotNull(entity.id), pageable)
            PageResponse(
                data = page.content.map(PromptTemplateRevisionMapper::toResponse),
                pageSize = request.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                empty = page.isEmpty,
                first = page.isFirst,
                last = page.isLast,
                size = page.numberOfElements,
                number = request.page,
            )
        }
    }

    override fun getRevision(id: Long, version: String): PromptTemplateRevisionResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        return read {
            val entity = promptTemplateRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw PromptTemplateNotFoundException()
            val revisionNumber = resolveRevision(version, entity.headRevision)
            val revision = promptTemplateRevisionRepository.findByTemplateIdAndRevisionNumber(
                requireNotNull(entity.id),
                revisionNumber,
            ) ?: throw PromptTemplateNotFoundException()
            PromptTemplateRevisionMapper.toResponse(revision)
        }
    }

    private fun commitMessage(message: String?, defaultWhenBlank: Boolean): String {
        val trimmed = message?.trim().orEmpty()
        if (trimmed.isEmpty() && defaultWhenBlank) return PromptTemplateLimits.INITIAL_COMMIT_MESSAGE
        return trimmed
    }

    private fun snapshotOf(entity: PromptTemplateEntity): PromptSnapshot = PromptSnapshot(
        groupName = entity.groupName,
        name = entity.name,
        engine = entity.engine,
        content = entity.content,
        description = entity.description,
        metadata = entity.metadata.duplicate(),
        tags = entity.tags.toList(),
    )

    private fun revision(
        templateId: Long,
        revisionNumber: Int,
        parentRevision: Int?,
        message: String,
        author: String,
        before: PromptSnapshot?,
        after: PromptSnapshot,
    ): PromptTemplateRevisionEntity = PromptTemplateRevisionMapper.toEntity(
        templateId = templateId,
        revisionNumber = revisionNumber,
        parentRevision = parentRevision,
        message = message,
        author = author,
        diff = PromptTemplateCommitDiff.render(before, after),
        snapshot = after,
    )

    private fun resolveRevision(version: String, headRevision: Int): Int {
        if (version == PromptTemplateVersions.LATEST) return headRevision
        val parsed = version.toIntOrNull()
        if (parsed == null || parsed < 1 || version != parsed.toString()) {
            throw InvalidPromptTemplateRequestException("The prompt template version is invalid.")
        }
        return parsed
    }

    private fun toPageable(request: PageRequest): org.springframework.data.domain.Pageable {
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

    private fun revisionPageable(request: PageRequest): org.springframework.data.domain.Pageable {
        if (request.size > MAX_PAGE_SIZE) {
            throw InvalidPromptTemplateRequestException(
                "The prompt template page size must be at most $MAX_PAGE_SIZE.",
            )
        }
        return SpringPageRequest.of(
            request.page - 1,
            request.size,
            Sort.by(Sort.Direction.DESC, "revisionNumber"),
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
        val ALLOWED_SORT_FIELDS = setOf("groupName", "name", "engine", "createdAt", "updatedAt")
    }
}
