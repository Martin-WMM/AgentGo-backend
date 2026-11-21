package com.agentgo.a2a.service.impl

import com.agentgo.a2a.embedding.DescriptionEmbedding
import com.agentgo.a2a.entity.A2aAgentEntity
import com.agentgo.a2a.exception.A2aAgentNotFoundException
import com.agentgo.a2a.exception.DuplicateA2aAgentNameException
import com.agentgo.a2a.exception.InvalidA2aAgentRequestException
import com.agentgo.a2a.mapper.A2aAgentMapper
import com.agentgo.a2a.repository.A2aAgentRepository
import com.agentgo.a2a.security.AuthenticatedUserIdResolver
import com.agentgo.a2a.service.A2aAgentRequestNormalizer
import com.agentgo.a2a.service.NormalizedA2aAgent
import com.agentgo.dto.a2a.A2aAgentCreateRequest
import com.agentgo.dto.a2a.A2aAgentResponse
import com.agentgo.dto.a2a.A2aAgentStatus
import com.agentgo.dto.a2a.A2aAgentUpdateRequest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.CallbackPreferringPlatformTransactionManager
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionCallback

class A2aAgentServiceImplTest {
    private val a2aAgentRepository = mock<A2aAgentRepository>()
    private val a2aAgentMapper = mock<A2aAgentMapper>()
    private val authenticatedUserIdResolver = mock<AuthenticatedUserIdResolver>()
    private val a2aAgentRequestNormalizer = mock<A2aAgentRequestNormalizer>()
    private val service = A2aAgentServiceImpl(
        a2aAgentRepository,
        a2aAgentMapper,
        authenticatedUserIdResolver,
        a2aAgentRequestNormalizer,
        PassThroughTransactionManager(),
    )

    @Test
    fun `creates an agent and stores the derived description vector`() {
        val request = A2aAgentCreateRequest()
        val normalized = sampleAgent(description = "helps with invoices")
        val entity = A2aAgentEntity(ownerUserId = "ada")
        val response = sampleResponse(11L)
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(a2aAgentRequestNormalizer.normalizeCreate(request)).thenReturn(normalized)
        whenever(
            a2aAgentRepository.existsByGroupNameAndNameAndCreator("billing", "billing-agent", "ada"),
        ).thenReturn(false)
        whenever(a2aAgentMapper.toEntity("ada", normalized)).thenReturn(entity)
        whenever(a2aAgentRepository.save(entity)).thenReturn(entity)
        whenever(a2aAgentMapper.toResponse(entity)).thenReturn(response)

        assertEquals(response, service.create(request))
        assertEquals(DescriptionEmbedding.embed("helps with invoices"), entity.descriptionVector)
        verify(a2aAgentRepository).flush()
    }

    @Test
    fun `rejects a duplicate group and name on create`() {
        val request = A2aAgentCreateRequest()
        val normalized = sampleAgent()
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(a2aAgentRequestNormalizer.normalizeCreate(request)).thenReturn(normalized)
        whenever(
            a2aAgentRepository.existsByGroupNameAndNameAndCreator("billing", "billing-agent", "ada"),
        ).thenReturn(true)

        assertFailsWith<DuplicateA2aAgentNameException> { service.create(request) }
        verify(a2aAgentRepository, never()).save(any())
    }

    @Test
    fun `does not persist when create validation fails`() {
        val request = A2aAgentCreateRequest()
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(a2aAgentRequestNormalizer.normalizeCreate(request))
            .thenThrow(InvalidA2aAgentRequestException("bad url"))

        assertFailsWith<InvalidA2aAgentRequestException> { service.create(request) }
        verify(a2aAgentRepository, never()).existsByGroupNameAndNameAndCreator(any(), any(), any())
    }

    @Test
    fun `gets one agent owned by the authenticated user`() {
        val entity = persistedEntity(4L)
        val response = sampleResponse(4L)
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(a2aAgentRepository.findByIdAndOwnerUserId(4L, "ada")).thenReturn(entity)
        whenever(a2aAgentMapper.toResponse(entity)).thenReturn(response)

        assertEquals(response, service.get(4L))
    }

    @Test
    fun `lists every owned agent or filters by status`() {
        val entity = persistedEntity(5L)
        val response = sampleResponse(5L)
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(a2aAgentRepository.findByOwnerUserIdOrderByUpdatedAtDesc("ada")).thenReturn(listOf(entity))
        whenever(
            a2aAgentRepository.findByOwnerUserIdAndStatusOrderByUpdatedAtDesc("ada", A2aAgentStatus.AVAILABLE),
        ).thenReturn(emptyList())
        whenever(a2aAgentMapper.toResponse(entity)).thenReturn(response)

        assertEquals(listOf(response), service.list(null))
        assertEquals(emptyList(), service.list(A2aAgentStatus.AVAILABLE))
    }

    @Test
    fun `updates an agent and recomputes the description vector`() {
        val id = 6L
        val entity = persistedEntity(id).apply {
            creator = "grace"
            descriptionVector = mutableListOf(1.0)
            enabled = true
            status = A2aAgentStatus.ERROR
        }
        val request = A2aAgentUpdateRequest()
        val normalized = sampleAgent(description = null, enabled = true, status = A2aAgentStatus.ERROR)
        val response = sampleResponse(id)
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(a2aAgentRequestNormalizer.normalizeUpdate(request)).thenReturn(normalized)
        whenever(a2aAgentRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(
            a2aAgentRepository.existsByGroupNameAndNameAndCreatorAndIdNot("billing", "billing-agent", "grace", id),
        ).thenReturn(false)
        stubApplyUpdate()
        whenever(a2aAgentRepository.save(entity)).thenReturn(entity)
        whenever(a2aAgentMapper.toResponse(entity)).thenReturn(response)

        assertEquals(response, service.update(id, request))
        assertEquals(DescriptionEmbedding.embed(null), entity.descriptionVector)
        assertEquals(A2aAgentStatus.ERROR, entity.status)
        verify(a2aAgentRepository).flush()
    }

    @Test
    fun `stores unavailable when an update disables an available agent`() {
        val id = 7L
        val entity = persistedEntity(id).apply {
            creator = " "
            enabled = true
            status = A2aAgentStatus.AVAILABLE
        }
        val normalized = sampleAgent(enabled = false, status = A2aAgentStatus.AVAILABLE)
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(a2aAgentRequestNormalizer.normalizeUpdate(any())).thenReturn(normalized)
        whenever(a2aAgentRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(
            a2aAgentRepository.existsByGroupNameAndNameAndCreatorAndIdNot("billing", "billing-agent", "ada", id),
        ).thenReturn(false)
        stubApplyUpdate()
        whenever(a2aAgentRepository.save(entity)).thenReturn(entity)
        whenever(a2aAgentMapper.toResponse(entity)).thenReturn(sampleResponse(id))

        service.update(id, A2aAgentUpdateRequest())

        assertEquals(false, entity.enabled)
        assertEquals(A2aAgentStatus.UNAVAILABLE, entity.status)
    }

    @Test
    fun `keeps available when an update leaves the agent enabled`() {
        val id = 8L
        val entity = persistedEntity(id).apply {
            creator = null
            status = A2aAgentStatus.UNKNOWN
        }
        val normalized = sampleAgent(enabled = true, status = A2aAgentStatus.AVAILABLE)
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(a2aAgentRequestNormalizer.normalizeUpdate(any())).thenReturn(normalized)
        whenever(a2aAgentRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(
            a2aAgentRepository.existsByGroupNameAndNameAndCreatorAndIdNot("billing", "billing-agent", "ada", id),
        ).thenReturn(false)
        stubApplyUpdate()
        whenever(a2aAgentRepository.save(entity)).thenReturn(entity)
        whenever(a2aAgentMapper.toResponse(entity)).thenReturn(sampleResponse(id))

        service.update(id, A2aAgentUpdateRequest())

        assertEquals(A2aAgentStatus.AVAILABLE, entity.status)
    }

    @Test
    fun `rejects a duplicate group and name on update`() {
        val id = 9L
        val entity = persistedEntity(id).apply { creator = "ada" }
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(a2aAgentRequestNormalizer.normalizeUpdate(any())).thenReturn(sampleAgent())
        whenever(a2aAgentRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(
            a2aAgentRepository.existsByGroupNameAndNameAndCreatorAndIdNot("billing", "billing-agent", "ada", id),
        ).thenReturn(true)

        assertFailsWith<DuplicateA2aAgentNameException> { service.update(id, A2aAgentUpdateRequest()) }
        verify(a2aAgentRepository, never()).save(any())
        verify(a2aAgentMapper, never()).applyUpdate(any(), any())
    }

    @Test
    fun `treats a missing owned agent as not found`() {
        val id = 10L
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(a2aAgentRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(null)
        whenever(a2aAgentRequestNormalizer.normalizeUpdate(any())).thenReturn(sampleAgent())

        assertFailsWith<A2aAgentNotFoundException> { service.get(id) }
        assertFailsWith<A2aAgentNotFoundException> { service.update(id, A2aAgentUpdateRequest()) }
        assertFailsWith<A2aAgentNotFoundException> { service.delete(id) }
        verify(a2aAgentRepository, never()).delete(any<A2aAgentEntity>())
    }

    @Test
    fun `deletes an agent owned by the authenticated user`() {
        val id = 12L
        val entity = persistedEntity(id)
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(a2aAgentRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)

        service.delete(id)

        verify(a2aAgentRepository).delete(entity)
    }

    @Test
    fun `deleteAll removes every owned agent and ignores duplicate ids`() {
        val first = persistedEntity(21L)
        val second = persistedEntity(22L)
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(a2aAgentRepository.findByIdAndOwnerUserId(21L, "ada")).thenReturn(first)
        whenever(a2aAgentRepository.findByIdAndOwnerUserId(22L, "ada")).thenReturn(second)
