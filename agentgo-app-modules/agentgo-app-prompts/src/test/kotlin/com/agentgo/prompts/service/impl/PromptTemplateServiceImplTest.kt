package com.agentgo.prompts.service.impl

import com.agentgo.commons.dto.http.request.PageRequest
import com.agentgo.commons.dto.http.request.SortOrder
import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.dto.prompts.PromptTemplateCreateRequest
import com.agentgo.dto.prompts.PromptTemplatePreviewRequest
import com.agentgo.dto.prompts.PromptTemplateRenderRequest
import com.agentgo.dto.prompts.PromptTemplateResponse
import com.agentgo.dto.prompts.PromptTemplateUpdateRequest
import com.agentgo.prompts.entity.PromptTemplateEntity
import com.agentgo.prompts.exception.DuplicatePromptTemplateNameException
import com.agentgo.prompts.exception.InvalidPromptTemplateRequestException
import com.agentgo.prompts.exception.PromptTemplateException
import com.agentgo.prompts.exception.PromptTemplateNotFoundException
import com.agentgo.prompts.mapper.PromptTemplateMapper
import com.agentgo.prompts.render.PromptTemplateEngine
import com.agentgo.prompts.repository.PromptTemplateRepository
import com.agentgo.prompts.security.AuthenticatedUserIdResolver
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus

class PromptTemplateServiceImplTest {
    private val repository = mock<PromptTemplateRepository>()
    private val mapper = mock<PromptTemplateMapper>()
    private val engine = mock<PromptTemplateEngine>()
    private val resolver = mock<AuthenticatedUserIdResolver>()
    private val service = PromptTemplateServiceImpl(
        repository,
        mapper,
        engine,
        resolver,
        PassThroughTransactionManager(),
    )

    @Test
    fun `create persists a normalized template for the authenticated owner`() {
        val request = createRequest("  Greeting  ", PromptEngine.SIMPLE, " Hello {{ name }} ", "  hi ")
        val normalized = createRequest("Greeting", PromptEngine.SIMPLE, "Hello {{ name }}", "hi")
        val entity = PromptTemplateEntity(ownerUserId = "ada", name = "Greeting")
        val saved = PromptTemplateEntity(id = UUID.randomUUID(), ownerUserId = "ada", name = "Greeting")
        val response = sampleResponse(saved.id!!, "Greeting")
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.existsByOwnerUserIdAndName("ada", "Greeting")).thenReturn(false)
        whenever(mapper.toEntity("ada", normalized)).thenReturn(entity)
        whenever(repository.save(entity)).thenReturn(saved)
        whenever(mapper.toResponse(saved)).thenReturn(response)

        assertEquals(response, service.create(request))
        verify(engine).validate(PromptEngine.SIMPLE, "Hello {{ name }}")
        verify(repository).save(entity)
    }

    @Test
    fun `create rejects a blank name a duplicate name and an unauthenticated caller`() {
        whenever(resolver.requireUserId()).thenReturn("ada")
        assertFailsWith<InvalidPromptTemplateRequestException> {
            service.create(createRequest("  ", PromptEngine.SIMPLE, "Hello"))
        }
        verify(repository, never()).save(any())

        whenever(repository.existsByOwnerUserIdAndName("ada", "Greeting")).thenReturn(true)
        assertFailsWith<DuplicatePromptTemplateNameException> {
            service.create(createRequest("Greeting", PromptEngine.SIMPLE, "Hello"))
        }

        whenever(resolver.requireUserId()).thenThrow(
            PromptTemplateException(1002, "AUTH-002", HttpStatus.UNAUTHORIZED, "Authentication is required."),
        )
        assertFailsWith<PromptTemplateException> {
            service.create(createRequest("Greeting", PromptEngine.SIMPLE, "Hello"))
        }
    }

    @Test
    fun `create rejects an oversized description and content`() {
        whenever(resolver.requireUserId()).thenReturn("ada")

        assertFailsWith<InvalidPromptTemplateRequestException> {
            service.create(
                createRequest(
                    "Greeting",
                    PromptEngine.SIMPLE,
                    "Hello",
                    description = "x".repeat(2001),
                ),
            )
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            service.create(
                createRequest(
                    "Greeting",
                    PromptEngine.SIMPLE,
                    "x".repeat(100_001),
                ),
            )
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            service.create(createRequest("n".repeat(256), PromptEngine.SIMPLE, "Hello"))
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            service.create(createRequest("Greeting", null, "Hello"))
        }
    }

    @Test
    fun `get and list stay scoped to the authenticated owner`() {
        val id = UUID.randomUUID()
        val entity = PromptTemplateEntity(id = id, ownerUserId = "ada", name = "Greeting")
        val response = sampleResponse(id, "Greeting")
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(mapper.toResponse(entity)).thenReturn(response)
        whenever(repository.findByOwnerUserId(eq("ada"), any())).thenReturn(PageImpl(listOf(entity)))

        assertEquals(response, service.get(id))
        val listed = service.list(PageRequest())
        assertEquals(listOf(response), listed.data)
        assertEquals(1, listed.number)
        assertEquals(20, listed.pageSize)
        assertEquals(1L, listed.totalElements)
        assertTrue(listed.first)
        assertTrue(listed.last)
        assertFalse(listed.empty)
        verify(repository).findByOwnerUserId(
            eq("ada"),
            check { pageable: Pageable ->
                assertEquals(0, pageable.pageNumber)
                assertEquals(20, pageable.pageSize)
                assertEquals(Sort.Direction.DESC, pageable.sort.getOrderFor("updatedAt")?.direction)
            },
        )
    }

    @Test
    fun `list applies the requested page and sort`() {
        val entity = PromptTemplateEntity(id = UUID.randomUUID(), ownerUserId = "ada", name = "Greeting")
        val response = sampleResponse(entity.id!!, "Greeting")
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.findByOwnerUserId(eq("ada"), any())).thenReturn(
            PageImpl(listOf(entity), org.springframework.data.domain.PageRequest.of(1, 5), 6),
        )
        whenever(mapper.toResponse(entity)).thenReturn(response)

        val listed = service.list(PageRequest(page = 2, size = 5, sort = listOf("name"), order = SortOrder.ASC))

        assertEquals(listOf(response), listed.data)
        assertEquals(2, listed.number)
        assertEquals(5, listed.pageSize)
        assertEquals(6L, listed.totalElements)
        assertEquals(2, listed.totalPages)
        assertFalse(listed.first)
        assertTrue(listed.last)
        verify(repository).findByOwnerUserId(
            eq("ada"),
            check { pageable: Pageable ->
                assertEquals(1, pageable.pageNumber)
                assertEquals(5, pageable.pageSize)
                assertEquals(Sort.Direction.ASC, pageable.sort.getOrderFor("name")?.direction)
            },
        )
    }

    @Test
    fun `list rejects an oversized page or an unknown sort field`() {
        whenever(resolver.requireUserId()).thenReturn("ada")

        assertFailsWith<InvalidPromptTemplateRequestException> {
            service.list(PageRequest(size = 101))
        }
        assertFailsWith<InvalidPromptTemplateRequestException> {
            service.list(PageRequest(sort = listOf("content")))
        }
        verify(repository, never()).findByOwnerUserId(any(), any())
    }

    @Test
    fun `get update and delete fail when the template belongs to another user`() {
        val id = UUID.randomUUID()
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.findByIdAndOwnerUserId(id, "ada")).thenReturn(null)

        assertFailsWith<PromptTemplateNotFoundException> { service.get(id) }
        assertFailsWith<PromptTemplateNotFoundException> {
            service.update(id, updateRequest("Greeting", PromptEngine.SIMPLE, "Hello"))
        }
        assertFailsWith<PromptTemplateNotFoundException> { service.delete(id) }
        verify(repository, never()).delete(any())
    }

    @Test
    fun `update replaces an owned template and delete removes it`() {
        val id = UUID.randomUUID()
        val entity = PromptTemplateEntity(id = id, ownerUserId = "ada", name = "Old")
        val update = updateRequest(" New ", PromptEngine.JINJA2, " Hello ", "  ")
        val normalized = updateRequest("New", PromptEngine.JINJA2, "Hello", null)
        val response = sampleResponse(id, "New")
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(repository.existsByOwnerUserIdAndNameAndIdNot(eq("ada"), eq("New"), eq(id)))
            .thenReturn(false)
        whenever(repository.save(entity)).thenReturn(entity)
        whenever(mapper.toResponse(entity)).thenReturn(response)

        assertEquals(response, service.update(id, update))
        verify(mapper).applyUpdate(entity, normalized)
        verify(engine).validate(PromptEngine.JINJA2, "Hello")

        service.delete(id)
        verify(repository).delete(entity)
    }

    @Test
    fun `update rejects a duplicate name for the same owner`() {
        val id = UUID.randomUUID()
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.findByIdAndOwnerUserId(id, "ada")).thenReturn(PromptTemplateEntity(id = id, ownerUserId = "ada"))
        whenever(repository.existsByOwnerUserIdAndNameAndIdNot("ada", "Greeting", id))
            .thenReturn(true)

