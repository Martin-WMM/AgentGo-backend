package com.agentgo.events.service.impl

import com.agentgo.commons.dto.http.request.PageRequest
import com.agentgo.commons.dto.http.request.SortOrder
import com.agentgo.commons.dto.http.response.PageResponse
import com.agentgo.dto.events.SystemEventCreateRequest
import com.agentgo.dto.events.SystemEventResponse
import com.agentgo.dto.events.SystemEventUpdateRequest
import com.agentgo.events.entity.SystemEventEntity
import com.agentgo.events.exception.InvalidSystemEventRequestException
import com.agentgo.events.exception.SystemEventNotFoundException
import com.agentgo.events.mapper.SystemEventMapper
import com.agentgo.events.repository.SystemEventRepository
import com.agentgo.events.retention.SystemEventRetentionPolicy
import com.agentgo.events.security.AuthenticatedUserIdResolver
import com.agentgo.events.service.SystemEventService
import java.time.Clock
import java.time.Instant
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest as SpringPageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * Default [SystemEventService] backed by JPA.
 *
 * Each public operation opens its own programmatic transaction. Field shape is
 * checked by Jakarta Bean Validation before this service runs. This class keeps
 * the authenticated actor, the retention window, and page rules.
 *
 * @property systemEventRepository event persistence access
 * @property systemEventMapper entity/DTO mapper
 * @property authenticatedUserIdResolver security-context user resolver
 * @property clock clock used for default event times and the retention cutoff
 */
@Service("systemEventServiceImpl")
class SystemEventServiceImpl(
    private val systemEventRepository: SystemEventRepository,
    private val systemEventMapper: SystemEventMapper,
    private val authenticatedUserIdResolver: AuthenticatedUserIdResolver,
    private val clock: Clock,
    transactionManager: PlatformTransactionManager,
) : SystemEventService {
    private val writeTransaction = TransactionTemplate(transactionManager)
    private val readTransaction = TransactionTemplate(transactionManager).apply {
        isReadOnly = true
    }

    override fun create(request: SystemEventCreateRequest): SystemEventResponse {
        val actor = authenticatedUserIdResolver.requireUserId()
        val now = clock.instant()
        val occurredAt = request.occurredAt ?: now
        ensureInsideWindow(occurredAt, now)
        return write {
            val saved = systemEventRepository.save(systemEventMapper.toEntity(actor, request, now))
            systemEventRepository.flush()
            ensureWithinCap(saved)
            purgeExpired(now)
            systemEventMapper.toResponse(saved)
        }
    }

    override fun get(id: Long): SystemEventResponse {
        authenticatedUserIdResolver.requireUserId()
        return read {
            systemEventMapper.toResponse(requireEvent(id))
        }
    }

    override fun list(
        request: PageRequest,
        domain: String?,
        operatorName: String?,
    ): PageResponse<SystemEventResponse> {
        authenticatedUserIdResolver.requireUserId()
        val pageable = toPageable(request)
        val filters = filters(domain, operatorName)
        return read {
            val page = if (filters == null) {
                systemEventRepository.findAll(pageable)
            } else {
                systemEventRepository.findAll(filters, pageable)
            }
            toPageResponse(page, request)
        }
    }

    override fun update(id: Long, request: SystemEventUpdateRequest): SystemEventResponse {
        val actor = authenticatedUserIdResolver.requireUserId()
        return write {
            val entity = requireEvent(id)
            systemEventMapper.applyUpdate(entity, actor, request)
            val now = clock.instant()
            ensureInsideWindow(entity.occurredAt, now)
            systemEventRepository.flush()
            ensureWithinCap(entity)
            purgeExpired(now)
            systemEventMapper.toResponse(entity)
        }
    }

    override fun delete(id: Long) {
        authenticatedUserIdResolver.requireUserId()
        write {
            systemEventRepository.delete(requireEvent(id))
        }
    }

    override fun enforceRetention() {
        purgeExpired(clock.instant())
    }

    private fun requireEvent(id: Long): SystemEventEntity =
        systemEventRepository.findById(id).orElseThrow { SystemEventNotFoundException() }

    private fun ensureInsideWindow(occurredAt: Instant, now: Instant) {
        if (occurredAt.isBefore(SystemEventRetentionPolicy.cutoff(now))) {
            throw InvalidSystemEventRequestException(
                "The event time is older than the three-month retention window.",
            )
        }
    }

    private fun ensureWithinCap(entity: SystemEventEntity) {
        val id = requireNotNull(entity.id) { "Persisted system event must have an identifier." }
        val newer = systemEventRepository.countNewerThan(entity.occurredAt, id)
        if (newer >= SystemEventRetentionPolicy.MAX_RETAINED_EVENTS) {
            throw InvalidSystemEventRequestException(
                "The event is outside the newest ${SystemEventRetentionPolicy.MAX_RETAINED_EVENTS} retained events.",
            )
        }
    }

    private fun purgeExpired(now: Instant) {
        write {
            systemEventRepository.deleteOlderThan(SystemEventRetentionPolicy.cutoff(now))
            var previous = Long.MAX_VALUE
            while (true) {
                val total = systemEventRepository.count()
                val excess = total - SystemEventRetentionPolicy.MAX_RETAINED_EVENTS
                if (excess <= 0 || total >= previous) break
                previous = total
                val batch = minOf(SystemEventRetentionPolicy.PURGE_BATCH_SIZE.toLong(), excess).toInt()
                val oldest = systemEventRepository.findAll(oldestPage(batch))
                if (oldest.isEmpty) break
                systemEventRepository.deleteAll(oldest.content)
                systemEventRepository.flush()
            }
        }
    }

    private fun filters(domain: String?, operatorName: String?): Specification<SystemEventEntity>? {
        val domainValue = domain?.trim()?.takeIf { it.isNotEmpty() }
        val operatorValue = operatorName?.trim()?.takeIf { it.isNotEmpty() }
        if (domainValue == null && operatorValue == null) return null
        return Specification { root, _, builder ->
            val predicates = buildList {
                if (domainValue != null) add(builder.equal(root.get<String>("domain"), domainValue))
                if (operatorValue != null) add(builder.equal(root.get<String>("operatorName"), operatorValue))
            }
            builder.and(*predicates.toTypedArray())
        }
    }

    private fun toPageable(request: PageRequest): org.springframework.data.domain.Pageable {
        if (request.size > SystemEventRetentionPolicy.MAX_PAGE_SIZE) {
            throw InvalidSystemEventRequestException(
                "The system event page size must be at most ${SystemEventRetentionPolicy.MAX_PAGE_SIZE}.",
            )
        }
        val requestedSort = request.sort.map { it.trim() }.filter { it.isNotEmpty() }
        val properties = requestedSort.map { field ->
            SORT_PROPERTIES[field] ?: throw InvalidSystemEventRequestException(
                "The system event sort field is not supported.",
            )
        }
        val sortFields = properties.ifEmpty { listOf(DEFAULT_SORT_PROPERTY) }
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

    private fun oldestPage(batch: Int): org.springframework.data.domain.Pageable =
        SpringPageRequest.of(
            0,
            batch,
            Sort.by(Sort.Order.asc("occurredAt"), Sort.Order.asc("id")),
        )

    private fun toPageResponse(
        page: Page<SystemEventEntity>,
        request: PageRequest,
    ): PageResponse<SystemEventResponse> =
        PageResponse(
            data = page.content.map(systemEventMapper::toResponse),
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
        readTransaction.execute { action() } ?: error("System event read transaction returned null")

    private fun <T> write(action: () -> T): T =
        writeTransaction.execute { action() } ?: error("System event write transaction returned null")

    private companion object {
        const val DEFAULT_SORT_PROPERTY = "occurredAt"
        val SORT_PROPERTIES = mapOf(
            "name" to "name",
            "domain" to "domain",
            "operator" to "operatorName",
            "occurredAt" to "occurredAt",
            "createdAt" to "createdAt",
        )
    }
}
