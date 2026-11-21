package com.agentgo.a2a.service.impl

import com.agentgo.a2a.embedding.DescriptionEmbedding
import com.agentgo.a2a.exception.A2aAgentNotFoundException
import com.agentgo.a2a.exception.InvalidA2aAgentRequestException
import com.agentgo.commons.dto.http.request.BatchIds
import com.agentgo.a2a.exception.DuplicateA2aAgentNameException
import com.agentgo.a2a.mapper.A2aAgentMapper
import com.agentgo.a2a.repository.A2aAgentRepository
import com.agentgo.a2a.security.AuthenticatedUserIdResolver
import com.agentgo.a2a.service.A2aAgentRequestNormalizer
import com.agentgo.a2a.service.A2aAgentService
import com.agentgo.dto.a2a.A2aAgentCreateRequest
import com.agentgo.dto.a2a.A2aAgentResponse
import com.agentgo.dto.a2a.A2aAgentStatus
import com.agentgo.dto.a2a.A2aAgentUpdateRequest
import com.agentgo.events.annotation.RecordSystemEvent
import com.agentgo.events.domain.SystemEventDomains
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * Default [A2aAgentService] implementation backed by JPA persistence.
 */
@Service
class A2aAgentServiceImpl(
    private val a2aAgentRepository: A2aAgentRepository,
    private val a2aAgentMapper: A2aAgentMapper,
    private val authenticatedUserIdResolver: AuthenticatedUserIdResolver,
    private val a2aAgentRequestNormalizer: A2aAgentRequestNormalizer,
    transactionManager: PlatformTransactionManager,
) : A2aAgentService {
    private val writeTransaction = TransactionTemplate(transactionManager)
    private val readTransaction = TransactionTemplate(transactionManager).apply {
        isReadOnly = true
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.A2A_AGENTS,
        name = "A2A agent created",
        comment = "Created A2A agent #{#result.groupName}/#{#result.name} (#{#result.id})",
    )
    override fun create(request: A2aAgentCreateRequest): A2aAgentResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val normalized = a2aAgentRequestNormalizer.normalizeCreate(request)
        return write {
            if (
                a2aAgentRepository.existsByGroupNameAndNameAndCreator(
                    normalized.groupName,
                    normalized.name,
                    ownerUserId,
                )
            ) {
                throw DuplicateA2aAgentNameException()
            }
            val entity = a2aAgentMapper.toEntity(ownerUserId, normalized)
            entity.descriptionVector = DescriptionEmbedding.embed(normalized.description).toMutableList()
            val saved = a2aAgentRepository.save(entity)
            a2aAgentRepository.flush()
            a2aAgentMapper.toResponse(saved)
        }
    }

    override fun get(id: Long): A2aAgentResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        return read {
            val entity = a2aAgentRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw A2aAgentNotFoundException()
            a2aAgentMapper.toResponse(entity)
        }
    }

    override fun list(status: A2aAgentStatus?): List<A2aAgentResponse> {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        return read {
            val entities = if (status == null) {
                a2aAgentRepository.findByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId)
            } else {
                a2aAgentRepository.findByOwnerUserIdAndStatusOrderByUpdatedAtDesc(ownerUserId, status)
            }
            entities.map(a2aAgentMapper::toResponse)
        }
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.A2A_AGENTS,
        name = "A2A agent updated",
        comment = "Updated A2A agent #{#result.groupName}/#{#result.name} (#{#id})",
    )
    override fun update(id: Long, request: A2aAgentUpdateRequest): A2aAgentResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val normalized = a2aAgentRequestNormalizer.normalizeUpdate(request)
        return write {
            val entity = a2aAgentRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw A2aAgentNotFoundException()
            val creator = entity.creator?.takeIf { it.isNotBlank() } ?: ownerUserId
            if (
                a2aAgentRepository.existsByGroupNameAndNameAndCreatorAndIdNot(
                    normalized.groupName,
                    normalized.name,
                    creator,
                    id,
                )
            ) {
                throw DuplicateA2aAgentNameException()
            }
            a2aAgentMapper.applyUpdate(entity, normalized)
            entity.descriptionVector = DescriptionEmbedding.embed(normalized.description).toMutableList()
            if (!entity.enabled && entity.status == A2aAgentStatus.AVAILABLE) {
                entity.status = A2aAgentStatus.UNAVAILABLE
            }
            val saved = a2aAgentRepository.save(entity)
            a2aAgentRepository.flush()
            a2aAgentMapper.toResponse(saved)
        }
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.A2A_AGENTS,
        name = "A2A agent deleted",
        comment = "Deleted A2A agent #{#id}",
    )
    override fun delete(id: Long) {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        write {
            val entity = a2aAgentRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw A2aAgentNotFoundException()
            a2aAgentRepository.delete(entity)
        }
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.A2A_AGENTS,
        name = "A2A agents deleted",
        comment = "Deleted #{#ids.size()} A2A agents",
    )
    override fun deleteAll(ids: List<Long>) {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val distinct = BatchIds.requireDistinctOr(ids) { InvalidA2aAgentRequestException(it) }
        write {
            val entities = distinct.map { id ->
                a2aAgentRepository.findByIdAndOwnerUserId(id, ownerUserId)
                    ?: throw A2aAgentNotFoundException()
            }
            entities.forEach(a2aAgentRepository::delete)
        }
    }

    private fun <T> read(action: () -> T): T =
        readTransaction.execute { action() } ?: error("A2A read transaction returned null")

    private fun <T> write(action: () -> T): T =
        writeTransaction.execute { action() } ?: error("A2A write transaction returned null")
}
