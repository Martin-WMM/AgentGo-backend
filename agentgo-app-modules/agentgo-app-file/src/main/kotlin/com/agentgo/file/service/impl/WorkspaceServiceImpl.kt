package com.agentgo.file.service.impl

import com.agentgo.dto.workspace.WorkspaceCreateRequest
import com.agentgo.dto.workspace.WorkspaceResponse
import com.agentgo.dto.workspace.WorkspaceUpdateRequest
import com.agentgo.file.exception.DuplicateWorkspaceNameException
import com.agentgo.file.exception.WorkspaceException
import com.agentgo.file.exception.WorkspaceNotEmptyException
import com.agentgo.file.exception.WorkspaceNotFoundException
import com.agentgo.file.mapper.WorkspaceMapper
import com.agentgo.file.repository.WorkspaceRepository
import com.agentgo.file.service.WorkspaceFileService
import com.agentgo.file.service.WorkspaceService
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Default [WorkspaceService] implementation backed by JPA persistence.
 *
 * Request field validation is enforced by Jakarta Bean Validation on the DTO/entity.
 * This service only applies ownership and business rules that cannot be expressed as
 * field constraints.
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
) : WorkspaceService {
    @Transactional
    override fun create(request: WorkspaceCreateRequest): WorkspaceResponse {
        val ownerUserId = requireUserId()
        val name = request.name.trim()
        if (workspaceRepository.existsByOwnerUserIdAndName(ownerUserId, name)) {
            throw DuplicateWorkspaceNameException()
        }
        val saved = workspaceRepository.save(workspaceMapper.toEntity(ownerUserId, request))
        return workspaceMapper.toResponse(saved)
    }

    @Transactional(readOnly = true)
    override fun get(id: UUID): WorkspaceResponse {
        val ownerUserId = requireUserId()
        val entity = workspaceRepository.findByIdAndOwnerUserId(id, ownerUserId)
            ?: throw WorkspaceNotFoundException()
        return workspaceMapper.toResponse(entity)
    }

    @Transactional(readOnly = true)
    override fun list(): List<WorkspaceResponse> {
        val ownerUserId = requireUserId()
        return workspaceRepository.findByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId)
            .map(workspaceMapper::toResponse)
    }

    @Transactional
    override fun update(id: UUID, request: WorkspaceUpdateRequest): WorkspaceResponse {
        val ownerUserId = requireUserId()
        val entity = workspaceRepository.findByIdAndOwnerUserId(id, ownerUserId)
            ?: throw WorkspaceNotFoundException()
        val name = request.name.trim()
        if (workspaceRepository.existsByOwnerUserIdAndNameAndIdNot(ownerUserId, name, id)) {
            throw DuplicateWorkspaceNameException()
        }
        workspaceMapper.applyUpdate(entity, request)
        return workspaceMapper.toResponse(workspaceRepository.save(entity))
    }

    @Transactional
    override fun delete(id: UUID) {
        val ownerUserId = requireUserId()
        val entity = workspaceRepository.findByIdAndOwnerUserId(id, ownerUserId)
            ?: throw WorkspaceNotFoundException()
        if (workspaceFileService.hasFilesInWorkspace(id)) {
            throw WorkspaceNotEmptyException()
        }
        workspaceRepository.delete(entity)
    }

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
}
