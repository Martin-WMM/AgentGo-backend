package com.agentgo.file.service.impl

import com.agentgo.commons.dto.http.request.BatchIds
import com.agentgo.dto.workspace.WorkspaceCreateRequest
import com.agentgo.dto.workspace.WorkspaceResponse
import com.agentgo.dto.workspace.WorkspaceUpdateRequest
import com.agentgo.events.annotation.RecordSystemEvent
import com.agentgo.events.domain.SystemEventDomains
import com.agentgo.file.exception.DuplicateWorkspaceNameException
import com.agentgo.file.exception.InvalidWorkspaceRequestException
import com.agentgo.file.exception.WorkspaceException
import com.agentgo.file.exception.WorkspaceNotEmptyException
import com.agentgo.file.exception.WorkspaceNotFoundException
import com.agentgo.file.mapper.WorkspaceMapper
import com.agentgo.file.repository.WorkspaceRepository
import com.agentgo.file.service.WorkspaceFileService
import com.agentgo.file.service.WorkspaceService
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * Default [WorkspaceService] implementation backed by JPA persistence.
 *
 * Request field validation is enforced by Jakarta Bean Validation on the DTO/entity.
 * This service only applies ownership and business rules that cannot be expressed as
 * field constraints. Each public operation opens its own programmatic transaction.
 *
 * @property workspaceRepository workspace persistence access
 * @property workspaceMapper entity/DTO mapper
 * @property workspaceFileService file occupancy checks for delete
 */
@Service
class WorkspaceServiceImpl(
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceMapper: WorkspaceMapper,
    private val workspaceFileService: WorkspaceFileService,
    transactionManager: PlatformTransactionManager,
) : WorkspaceService {
    private val writeTransaction = TransactionTemplate(transactionManager)
    private val readTransaction = TransactionTemplate(transactionManager).apply {
        isReadOnly = true
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.WORKSPACES,
        name = "Workspace created",
        comment = "Created workspace #{#result.groupName}/#{#result.name} (#{#result.id})",
    )
    override fun create(request: WorkspaceCreateRequest): WorkspaceResponse {
        val ownerUserId = requireUserId()
        val groupName = normalizeGroup(request.groupName)
        val name = request.name.trim()
        return write {
            if (workspaceRepository.existsByGroupNameAndNameAndCreator(groupName, name, ownerUserId)) {
                throw DuplicateWorkspaceNameException()
            }
            val saved = workspaceRepository.save(workspaceMapper.toEntity(ownerUserId, request))
            workspaceRepository.flush()
            workspaceMapper.toResponse(saved)
        }
    }

    override fun get(id: Long): WorkspaceResponse {
        val ownerUserId = requireUserId()
        return read {
            val entity = workspaceRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw WorkspaceNotFoundException()
            workspaceMapper.toResponse(entity)
        }
    }

    override fun list(): List<WorkspaceResponse> {
        val ownerUserId = requireUserId()
        return read {
            workspaceRepository.findByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId)
                .map(workspaceMapper::toResponse)
        }
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.WORKSPACES,
        name = "Workspace updated",
        comment = "Updated workspace #{#result.groupName}/#{#result.name} (#{#id})",
    )
    override fun update(id: Long, request: WorkspaceUpdateRequest): WorkspaceResponse {
        val ownerUserId = requireUserId()
        val groupName = normalizeGroup(request.groupName)
        val name = request.name.trim()
        return write {
            val entity = workspaceRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw WorkspaceNotFoundException()
            val creator = entity.creator?.takeIf { it.isNotBlank() } ?: ownerUserId
            if (
                workspaceRepository.existsByGroupNameAndNameAndCreatorAndIdNot(
                    groupName,
                    name,
                    creator,
                    id,
                )
            ) {
                throw DuplicateWorkspaceNameException()
            }
            workspaceMapper.applyUpdate(entity, request)
            val saved = workspaceRepository.save(entity)
            workspaceRepository.flush()
            workspaceMapper.toResponse(saved)
        }
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.WORKSPACES,
        name = "Workspace deleted",
        comment = "Deleted workspace #{#id}",
    )
    override fun delete(id: Long) {
        val ownerUserId = requireUserId()
        write {
            val entity = workspaceRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw WorkspaceNotFoundException()
            if (workspaceFileService.hasFilesInWorkspace(id)) {
                throw WorkspaceNotEmptyException()
            }
            workspaceRepository.delete(entity)
        }
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.WORKSPACES,
        name = "Workspaces deleted",
        comment = "Deleted #{#ids.size()} workspaces",
    )
    override fun deleteAll(ids: List<Long>) {
        val ownerUserId = requireUserId()
        val distinct = BatchIds.requireDistinctOr(ids) { InvalidWorkspaceRequestException(it) }
        write {
            val entities = distinct.map { id ->
                workspaceRepository.findByIdAndOwnerUserId(id, ownerUserId)
                    ?: throw WorkspaceNotFoundException()
            }
            if (entities.any { workspaceFileService.hasFilesInWorkspace(requireNotNull(it.id)) }) {
                throw WorkspaceNotEmptyException()
            }
            entities.forEach(workspaceRepository::delete)
        }
    }

    private fun normalizeGroup(value: String): String = value.trim().ifEmpty { "default" }

    private fun requireUserId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (
            authentication == null ||
            !authentication.isAuthenticated ||
            authentication is AnonymousAuthenticationToken ||
            authentication.name.isNullOrBlank()
        ) {
            throw WorkspaceException(
                code = 1002,
                responseType = "AUTH-002",
                status = HttpStatus.UNAUTHORIZED,
                message = "Authentication is required.",
            )
        }
        return authentication.name
    }

    private fun <T> read(action: () -> T): T =
        readTransaction.execute { action() } ?: error("Workspace read transaction returned null")

    private fun <T> write(action: () -> T): T =
        writeTransaction.execute { action() } ?: error("Workspace write transaction returned null")
}
