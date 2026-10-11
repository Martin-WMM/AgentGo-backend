package com.agentgo.file.mapper

import com.agentgo.dto.workspace.WorkspaceColors
import com.agentgo.dto.workspace.WorkspaceCreateRequest
import com.agentgo.dto.workspace.WorkspaceMetadata
import com.agentgo.dto.workspace.WorkspaceResponse
import com.agentgo.dto.workspace.WorkspaceUpdateRequest
import com.agentgo.file.entity.WorkspaceEntity
import org.springframework.stereotype.Component

/** Default [WorkspaceMapper] implementation used by the workspace service layer. */
@Component
class WorkspaceMapperImpl : WorkspaceMapper {
    override fun toResponse(entity: WorkspaceEntity): WorkspaceResponse =
        WorkspaceResponse(
            id = requireNotNull(entity.id) { "Persisted workspace must have an identifier." },
            name = entity.name,
            description = entity.description,
            metadata = metadataWith(readColor(entity.metadata?.color)),
            createdAt = requireNotNull(entity.createdAt) { "Persisted workspace must have a creation timestamp." },
            updatedAt = requireNotNull(entity.updatedAt) { "Persisted workspace must have an update timestamp." },
        )

    override fun toEntity(ownerUserId: String, request: WorkspaceCreateRequest): WorkspaceEntity =
        WorkspaceEntity(
            ownerUserId = ownerUserId.trim(),
            name = request.name.trim(),
            description = normalizeDescription(request.description),
            metadata = metadataWith(writeColor(request.metadata?.color)),
        )

    override fun applyUpdate(entity: WorkspaceEntity, request: WorkspaceUpdateRequest) {
        entity.name = request.name.trim()
        entity.description = normalizeDescription(request.description)
        if (request.metadata != null) {
            entity.metadata = metadataWith(writeColor(request.metadata?.color))
        } else if (entity.metadata?.color.isNullOrBlank()) {
            entity.metadata = metadataWith(WorkspaceColors.DEFAULT)
        }
    }

    private fun normalizeDescription(description: String?): String? =
        description?.trim()?.takeIf { it.isNotEmpty() }

    private fun writeColor(color: String?): String {
        if (color.isNullOrBlank()) return WorkspaceColors.DEFAULT
        return WorkspaceColors.canonicalize(color)
            ?: throw IllegalArgumentException("Unsupported workspace color.")
    }

    private fun readColor(color: String?): String =
        color?.let(WorkspaceColors::canonicalize) ?: WorkspaceColors.DEFAULT

    private fun metadataWith(color: String): WorkspaceMetadata =
        WorkspaceMetadata().apply { this.color = color }
}
