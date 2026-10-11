package com.agentgo.file.service.impl

import com.agentgo.dto.workspace.WorkspaceCreateRequest
import com.agentgo.dto.workspace.WorkspaceResponse
import com.agentgo.dto.workspace.WorkspaceUpdateRequest
import com.agentgo.file.entity.WorkspaceEntity
import com.agentgo.file.exception.DuplicateWorkspaceNameException
import com.agentgo.file.exception.WorkspaceException
import com.agentgo.file.exception.WorkspaceNotEmptyException
import com.agentgo.file.exception.WorkspaceNotFoundException
import com.agentgo.file.mapper.WorkspaceMapper
import com.agentgo.file.repository.WorkspaceRepository
import com.agentgo.file.service.WorkspaceFileService
import java.time.Instant
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class WorkspaceServiceImplTest {
    private val workspaceRepository = mock<WorkspaceRepository>()
    private val workspaceMapper = mock<WorkspaceMapper>()
    private val workspaceFileService = mock<WorkspaceFileService>()
    private val service = WorkspaceServiceImpl(workspaceRepository, workspaceMapper, workspaceFileService)

    @AfterTest
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `create persists workspace for authenticated owner`() {
        authenticateAs("ada")
        val request = WorkspaceCreateRequest().apply {
            name = "Reports"
            description = "Quarterly drafts"
        }
        val entity = WorkspaceEntity(ownerUserId = "ada", name = request.name, description = request.description)
        val saved = entity.withId(UUID.randomUUID())
        val response = sampleResponse(saved.id!!, request.name, request.description)
        whenever(workspaceRepository.existsByOwnerUserIdAndName("ada", "Reports")).thenReturn(false)
        whenever(workspaceMapper.toEntity("ada", request)).thenReturn(entity)
        whenever(workspaceRepository.save(entity)).thenReturn(saved)
        whenever(workspaceMapper.toResponse(saved)).thenReturn(response)

        assertEquals(response, service.create(request))
        verify(workspaceRepository).save(entity)
    }

    @Test
    fun `create rejects duplicate workspace name for same owner`() {
        authenticateAs("ada")
        whenever(workspaceRepository.existsByOwnerUserIdAndName("ada", "Reports")).thenReturn(true)

        assertFailsWith<DuplicateWorkspaceNameException> {
            service.create(WorkspaceCreateRequest().apply { name = "Reports" })
        }
        verify(workspaceRepository, never()).save(any())
    }

    @Test
    fun `create requires authentication`() {
        SecurityContextHolder.clearContext()

        val exception = assertFailsWith<WorkspaceException> {
            service.create(WorkspaceCreateRequest().apply { name = "Reports" })
        }

        assertEquals(1002, exception.code)
        assertEquals("AUTH-002", exception.responseType)
        assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
        verify(workspaceRepository, never()).save(any())
    }

    @Test
    fun `get returns workspace owned by authenticated user`() {
        authenticateAs("ada")
        val id = UUID.randomUUID()
        val entity = WorkspaceEntity(
            id = id,
            ownerUserId = "ada",
            name = "Reports",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
        val response = sampleResponse(id, "Reports", null)
        whenever(workspaceRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(workspaceMapper.toResponse(entity)).thenReturn(response)

        assertEquals(response, service.get(id))
        verify(workspaceRepository).findByIdAndOwnerUserId(id, "ada")
    }

    @Test
    fun `get throws not found when workspace is missing for owner`() {
        authenticateAs("ada")
        val id = UUID.randomUUID()
        whenever(workspaceRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(null)

        assertFailsWith<WorkspaceNotFoundException> { service.get(id) }
    }

    @Test
    fun `list returns owner scoped workspaces ordered by repository`() {
        authenticateAs("ada")
        val first = WorkspaceEntity(id = UUID.randomUUID(), ownerUserId = "ada", name = "A")
        val second = WorkspaceEntity(id = UUID.randomUUID(), ownerUserId = "ada", name = "B")
        val firstResponse = sampleResponse(first.id!!, "A", null)
        val secondResponse = sampleResponse(second.id!!, "B", null)
        whenever(workspaceRepository.findByOwnerUserIdOrderByUpdatedAtDesc("ada")).thenReturn(listOf(first, second))
        whenever(workspaceMapper.toResponse(first)).thenReturn(firstResponse)
        whenever(workspaceMapper.toResponse(second)).thenReturn(secondResponse)

        assertEquals(listOf(firstResponse, secondResponse), service.list())
        verify(workspaceRepository).findByOwnerUserIdOrderByUpdatedAtDesc("ada")
    }

    @Test
    fun `update applies mapper changes and saves owned workspace`() {
        authenticateAs("ada")
        val id = UUID.randomUUID()
        val entity = WorkspaceEntity(id = id, ownerUserId = "ada", name = "old")
        val update = WorkspaceUpdateRequest().apply {
            name = "new"
            description = "desc"
        }
        val response = sampleResponse(id, "new", "desc")
        whenever(workspaceRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(workspaceRepository.existsByOwnerUserIdAndNameAndIdNot("ada", "new", id)).thenReturn(false)
        whenever(workspaceRepository.save(entity)).thenReturn(entity)
        whenever(workspaceMapper.toResponse(entity)).thenReturn(response)

        assertEquals(response, service.update(id, update))
        verify(workspaceMapper).applyUpdate(eq(entity), any())
        verify(workspaceRepository).save(entity)
    }

    @Test
    fun `update rejects duplicate name against another owned workspace`() {
        authenticateAs("ada")
        val id = UUID.randomUUID()
        val entity = WorkspaceEntity(id = id, ownerUserId = "ada", name = "old")
        whenever(workspaceRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(workspaceRepository.existsByOwnerUserIdAndNameAndIdNot("ada", "new", id)).thenReturn(true)

        assertFailsWith<DuplicateWorkspaceNameException> {
            service.update(
                id,
                WorkspaceUpdateRequest().apply {
                    name = "new"
                },
            )
        }
        verify(workspaceRepository, never()).save(any())
    }

    @Test
    fun `delete removes empty owned workspace`() {
        authenticateAs("ada")
        val id = UUID.randomUUID()
        val entity = WorkspaceEntity(id = id, ownerUserId = "ada", name = "Reports")
        whenever(workspaceRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(workspaceFileService.hasFilesInWorkspace(id)).thenReturn(false)

        service.delete(id)

        verify(workspaceRepository).delete(entity)
    }

    @Test
    fun `delete rejects workspace that still contains files`() {
        authenticateAs("ada")
        val id = UUID.randomUUID()
        val entity = WorkspaceEntity(id = id, ownerUserId = "ada", name = "Reports")
        whenever(workspaceRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(workspaceFileService.hasFilesInWorkspace(id)).thenReturn(true)

        assertFailsWith<WorkspaceNotEmptyException> { service.delete(id) }
        verify(workspaceRepository, never()).delete(any<WorkspaceEntity>())
    }

    @Test
    fun `delete throws not found when workspace is missing for owner`() {
        authenticateAs("ada")
        val id = UUID.randomUUID()
        whenever(workspaceRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(null)

        assertFailsWith<WorkspaceNotFoundException> { service.delete(id) }
        verify(workspaceFileService, never()).hasFilesInWorkspace(any())
    }

    private fun authenticateAs(userId: String) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(userId, "n/a", emptyList())
    }

    private fun sampleResponse(id: UUID, name: String, description: String?): WorkspaceResponse =
        WorkspaceResponse(
            id = id,
            name = name,
            description = description,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-01-02T00:00:00Z"),
        )

    private fun WorkspaceEntity.withId(id: UUID): WorkspaceEntity =
        WorkspaceEntity(
            id = id,
            ownerUserId = ownerUserId,
            name = name,
            description = description,
            metadata = metadata,
            creator = creator,
            createdAt = createdAt ?: Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = updatedAt ?: Instant.parse("2026-01-01T00:00:00Z"),
            updatedBy = updatedBy,
        )
}
