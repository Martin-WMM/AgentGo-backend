package com.agentgo.models.service.impl

import com.agentgo.dto.models.ModelCreateRequest
import com.agentgo.dto.models.ModelResponse
import com.agentgo.dto.models.ModelType
import com.agentgo.dto.models.ModelUpdateRequest
import com.agentgo.models.entity.ModelEntity
import com.agentgo.models.exception.DuplicateModelNameException
import com.agentgo.models.exception.InvalidModelRequestException
import com.agentgo.models.exception.ModelNotFoundException
import com.agentgo.models.mapper.ModelMapper
import com.agentgo.models.repository.ModelRepository
import com.agentgo.models.security.AuthenticatedUserIdResolver
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ModelServiceImplTest {
    private val modelRepository = mock<ModelRepository>()
    private val modelMapper = mock<ModelMapper>()
    private val authenticatedUserIdResolver = mock<AuthenticatedUserIdResolver>()
    private val service = ModelServiceImpl(modelRepository, modelMapper, authenticatedUserIdResolver)

    @Test
    fun createsModelForAuthenticatedOwner() {
        val request = ModelCreateRequest("gpt-4o-mini", "openai", ModelType.LLM, mapOf("model" to "gpt-4o-mini"))
        val entity = ModelEntity(ownerUserId = "ada", name = request.name, provider = request.provider, type = request.type)
        val saved = entity.copyWithId(UUID.randomUUID())
        val response = sampleResponse(saved.id!!, request.name, request.type)
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(modelRepository.existsByOwnerUserIdAndName("ada", "gpt-4o-mini")).thenReturn(false)
        whenever(modelMapper.toEntity("ada", request)).thenReturn(entity)
        whenever(modelRepository.save(entity)).thenReturn(saved)
        whenever(modelMapper.toResponse(saved)).thenReturn(response)

        assertEquals(response, service.create(request))
        verify(modelRepository).save(entity)
    }

    @Test
    fun rejectsBlankNameOnCreate() {
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")

        assertFailsWith<InvalidModelRequestException> {
            service.create(ModelCreateRequest("  ", "openai", ModelType.LLM))
        }
        verify(modelRepository, never()).save(any())
    }

    @Test
    fun rejectsDuplicateNameOnCreate() {
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(modelRepository.existsByOwnerUserIdAndName("ada", "gpt-4o-mini")).thenReturn(true)

        assertFailsWith<DuplicateModelNameException> {
            service.create(ModelCreateRequest("gpt-4o-mini", "openai", ModelType.LLM))
        }
    }

    @Test
    fun getsAndListsOnlyOwnerScopedModels() {
        val id = UUID.randomUUID()
        val entity = ModelEntity(id = id, ownerUserId = "ada", name = "gpt", type = ModelType.LLM, createdAt = Instant.now(), updatedAt = Instant.now())
        val response = sampleResponse(id, "gpt", ModelType.LLM)
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(modelRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(modelMapper.toResponse(entity)).thenReturn(response)
        whenever(modelRepository.findByOwnerUserIdAndTypeOrderByUpdatedAtDesc("ada", ModelType.LLM))
            .thenReturn(listOf(entity))

        assertEquals(response, service.get(id))
        assertEquals(listOf(response), service.list(ModelType.LLM))
    }

    @Test
    fun updatesAndDeletesOwnedModels() {
        val id = UUID.randomUUID()
        val entity = ModelEntity(id = id, ownerUserId = "ada", name = "old", type = ModelType.LLM)
        val update = ModelUpdateRequest("new", "ollama", ModelType.EMBEDDING, mapOf("model" to "nomic"), "desc")
        val response = sampleResponse(id, "new", ModelType.EMBEDDING)
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(modelRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(modelRepository.existsByOwnerUserIdAndNameAndIdNot("ada", "new", id)).thenReturn(false)
        whenever(modelRepository.save(entity)).thenReturn(entity)
        whenever(modelMapper.toResponse(entity)).thenReturn(response)

        assertEquals(response, service.update(id, update))
        verify(modelMapper).applyUpdate(eq(entity), any())

        service.delete(id)
        verify(modelRepository).delete(entity)
    }

    @Test
    fun treatsMissingOwnedModelAsNotFound() {
        val id = UUID.randomUUID()
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(modelRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(null)

        assertFailsWith<ModelNotFoundException> { service.get(id) }
        assertFailsWith<ModelNotFoundException> {
            service.update(id, ModelUpdateRequest("n", "p", ModelType.OTHER))
        }
        assertFailsWith<ModelNotFoundException> { service.delete(id) }
    }

    private fun sampleResponse(id: UUID, name: String, type: ModelType): ModelResponse =
        ModelResponse(
            id = id,
            name = name,
            provider = "openai",
            type = type,
            configuration = emptyMap(),
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-01-02T00:00:00Z"),
        )

    private fun ModelEntity.copyWithId(id: UUID): ModelEntity =
        ModelEntity(
            id = id,
            ownerUserId = ownerUserId,
            name = name,
            provider = provider,
            type = type,
            configuration = configuration,
            description = description,
            creator = creator,
            createdAt = createdAt ?: Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = updatedAt ?: Instant.parse("2026-01-01T00:00:00Z"),
            updatedBy = updatedBy,
        )
}
