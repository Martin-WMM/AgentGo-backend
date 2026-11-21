package com.agentgo.prompts.service.impl

import com.agentgo.commons.dto.http.request.PageRequest
import com.agentgo.commons.dto.http.request.SortOrder
import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.dto.prompts.PromptTemplateCreateRequest
import com.agentgo.dto.prompts.PromptTemplateMetadata
import com.agentgo.dto.prompts.PromptTemplatePreviewRequest
import com.agentgo.dto.prompts.PromptTemplateRenderRequest
import com.agentgo.dto.prompts.PromptTemplateResponse
import com.agentgo.dto.prompts.PromptTemplateUpdateRequest
import com.agentgo.dto.prompts.PromptTemplateVersions
import com.agentgo.dto.workspace.WorkspaceColors
import com.agentgo.prompts.entity.PromptTemplateEntity
import com.agentgo.prompts.entity.PromptTemplateRevisionEntity
import com.agentgo.prompts.exception.DuplicatePromptTemplateNameException
import com.agentgo.prompts.exception.InvalidPromptTemplateRequestException
import com.agentgo.prompts.exception.PromptTemplateException
import com.agentgo.prompts.exception.PromptTemplateNotFoundException
import com.agentgo.prompts.mapper.PromptTemplateMapperImpl
import com.agentgo.prompts.render.PromptTemplateEngine
import com.agentgo.prompts.repository.PromptTemplateRepository
import com.agentgo.prompts.repository.PromptTemplateRevisionRepository
import com.agentgo.prompts.security.AuthenticatedUserIdResolver
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
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
    private val revisionRepository = mock<PromptTemplateRevisionRepository>()
    private val mapper = PromptTemplateMapperImpl()
    private val engine = mock<PromptTemplateEngine>()
    private val resolver = mock<AuthenticatedUserIdResolver>()
    private val service = PromptTemplateServiceImpl(
        repository,
        revisionRepository,
        mapper,
        engine,
        resolver,
        PassThroughTransactionManager(),
    )

    @Test
    fun `create persists a normalized template for the authenticated owner`() {
        val request = createRequest("  Greeting  ", PromptEngine.SIMPLE, " Hello {{ name }} ", "  hi ")
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.existsByGroupNameAndNameAndCreator("chat", "Greeting", "ada")).thenReturn(false)
        whenever(repository.save(any())).thenAnswer { stamp(it.arguments[0] as PromptTemplateEntity) }

        val response = service.create(request)

        assertEquals("Greeting", response.name)
        assertEquals("Hello {{ name }}", response.content)
        assertEquals("hi", response.description)
        assertEquals(WorkspaceColors.DEFAULT, response.metadata.color)
        assertEquals(PromptTemplateVersions.LATEST, response.version)
        assertEquals(1, response.revision)
        verify(engine).validate(PromptEngine.SIMPLE, "Hello {{ name }}")
        verify(repository).save(any())
        val revision = argumentCaptor<PromptTemplateRevisionEntity>()
        verify(revisionRepository).save(revision.capture())
        assertEquals(1, revision.firstValue.revisionNumber)
        assertEquals("Initial commit", revision.firstValue.message)
        assertEquals("ada", revision.firstValue.author)
        assertTrue(revision.firstValue.diff.contains("+Hello {{ name }}"))
    }

    @Test
    fun `create rejects a duplicate name and an unauthenticated caller`() {
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.existsByGroupNameAndNameAndCreator("chat", "Greeting", "ada")).thenReturn(true)
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
    fun `get and list stay scoped to the authenticated owner`() {
        val id = 1L
        val entity = stored("Greeting", id)
        val response = sampleResponse(id, "Greeting")
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
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
        val entity = stored("Greeting")
        val response = sampleResponse(entity.id!!, "Greeting")
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.findByOwnerUserId(eq("ada"), any())).thenReturn(
            PageImpl(listOf(entity), org.springframework.data.domain.PageRequest.of(1, 5), 6),
        )

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
        val id = 2L
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
        val id = 3L
        val entity = PromptTemplateEntity(
            id = id,
            ownerUserId = "ada",
            name = "Old",
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-01-02T00:00:00Z"),
        )
        val update = updateRequest(" New ", PromptEngine.JINJA2, " Hello ", "  ")
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(repository.existsByGroupNameAndNameAndCreatorAndIdNot(eq("chat"), eq("New"), eq("ada"), eq(id)))
            .thenReturn(false)
        whenever(repository.save(any())).thenAnswer { it.arguments[0] }

        val updated = service.update(id, update)

        assertEquals("New", updated.name)
        assertEquals(PromptEngine.JINJA2, updated.engine)
        assertEquals("Hello", updated.content)
        assertEquals(null, updated.description)
        assertEquals(2, updated.revision)
        verify(engine).validate(PromptEngine.JINJA2, "Hello")

        service.delete(id)
        verify(repository).delete(entity)
    }

    @Test
    fun `deleteAll removes templates and their revisions`() {
        val first = PromptTemplateEntity(id = 8L, ownerUserId = "ada", name = "One")
        val second = PromptTemplateEntity(id = 9L, ownerUserId = "ada", name = "Two")
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.findByIdAndOwnerUserId(8L, "ada")).thenReturn(first)
        whenever(repository.findByIdAndOwnerUserId(9L, "ada")).thenReturn(second)

        service.deleteAll(listOf(8L, 9L))

        verify(revisionRepository).deleteByTemplateId(8L)
        verify(revisionRepository).deleteByTemplateId(9L)
        verify(repository).delete(first)
        verify(repository).delete(second)
    }

    @Test
    fun `deleteAll deletes nothing when any template is missing or the list is empty`() {
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.findByIdAndOwnerUserId(8L, "ada")).thenReturn(PromptTemplateEntity(id = 8L, ownerUserId = "ada"))
        whenever(repository.findByIdAndOwnerUserId(10L, "ada")).thenReturn(null)

        assertFailsWith<PromptTemplateNotFoundException> { service.deleteAll(listOf(8L, 10L)) }
        assertFailsWith<InvalidPromptTemplateRequestException> { service.deleteAll(emptyList()) }
        verify(repository, never()).delete(any<PromptTemplateEntity>())
        verify(revisionRepository, never()).deleteByTemplateId(any<Long>())
    }

    @Test
    fun `update rejects a duplicate name for the same owner`() {
        val id = 4L
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.findByIdAndOwnerUserId(id, "ada")).thenReturn(PromptTemplateEntity(id = id, ownerUserId = "ada"))
        whenever(repository.existsByGroupNameAndNameAndCreatorAndIdNot("chat", "Greeting", "ada", id))
            .thenReturn(true)

        assertFailsWith<DuplicatePromptTemplateNameException> {
            service.update(id, updateRequest("Greeting", PromptEngine.SIMPLE, "Hello"))
        }
        verify(repository, never()).save(any())
    }

    @Test
    fun `preview renders unsaved content and render uses the stored template`() {
        val id = 5L
        val entity = PromptTemplateEntity(
            id = id,
            ownerUserId = "ada",
            engine = PromptEngine.SIMPLE,
            content = "Hello {{ name }}",
        )
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(engine.render(PromptEngine.JINJA2, "Hi {{ name }}", mapOf("name" to "Ada"))).thenReturn("Hi Ada")
        whenever(repository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(engine.render(PromptEngine.SIMPLE, "Hello {{ name }}", mapOf("name" to "Ada"))).thenReturn("Hello Ada")

        val preview = service.preview(
            previewRequest(PromptEngine.JINJA2, " Hi {{ name }} ", mapOf("name" to "Ada")),
        )
        val rendered = service.render(id, renderRequest(mapOf("name" to "Ada")))

        assertEquals("Hi Ada", preview.output)
        assertEquals("Hello Ada", rendered.output)
        verify(engine).validate(PromptEngine.JINJA2, "Hi {{ name }}")
    }

    @Test
    fun `render fails when the stored template is not owned by the caller`() {
        val id = 6L
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.findByIdAndOwnerUserId(id, "ada")).thenReturn(null)

        assertFailsWith<PromptTemplateNotFoundException> {
            service.render(id, renderRequest(mapOf("name" to "Ada")))
        }
        verify(engine, never()).render(any(), any(), any())
    }

    @Test
    fun `update rejects an unchanged template`() {
        val id = 7L
        whenever(resolver.requireUserId()).thenReturn("ada")
        val unchanged = PromptTemplateEntity(
            id = id,
            ownerUserId = "ada",
            groupName = "chat",
            name = "Greeting",
            engine = PromptEngine.SIMPLE,
            content = "Hello",
            metadata = defaultMetadata(),
        )
        whenever(repository.findByIdAndOwnerUserId(id, "ada")).thenReturn(unchanged)
        whenever(repository.existsByGroupNameAndNameAndCreatorAndIdNot("chat", "Greeting", "ada", id)).thenReturn(false)
        assertFailsWith<InvalidPromptTemplateRequestException> {
            service.update(id, updateRequest("Greeting", PromptEngine.SIMPLE, "Hello"))
        }
        verify(repository, never()).save(any())
    }

    @Test
    fun `list revisions returns the newest page and get resolves latest`() {
        val id = 8L
        val template = PromptTemplateEntity(id = id, ownerUserId = "ada", name = "Greeting", headRevision = 2)
        val revision = PromptTemplateRevisionEntity(
            id = 9L,
            templateId = id,
            revisionNumber = 2,
            parentRevisionNumber = 1,
            message = "Tighten",
            author = "ada",
            diff = "diff",
            groupName = "chat",
            name = "Greeting",
            content = "Hello",
            createdAt = Instant.parse("2026-01-03T00:00:00Z"),
        )
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.findByIdAndOwnerUserId(id, "ada")).thenReturn(template)
        whenever(revisionRepository.findByTemplateId(eq(id), any())).thenReturn(PageImpl(listOf(revision)))
        whenever(revisionRepository.findByTemplateIdAndRevisionNumber(id, 2)).thenReturn(revision)

        val listed = service.listRevisions(id, PageRequest())
        val loaded = service.getRevision(id, "latest")

        assertEquals("2", listed.data.single().version)
        assertEquals("Tighten", loaded.message)
        assertEquals(1, loaded.parentRevision)
        assertFailsWith<InvalidPromptTemplateRequestException> {
            service.getRevision(id, "v1")
        }
    }

    private fun createRequest(
        name: String,
        engine: PromptEngine?,
        content: String,
        description: String? = null,
        groupName: String = "chat",
        message: String? = null,
        tags: List<String>? = null,
        metadata: PromptTemplateMetadata? = null,
    ) = PromptTemplateCreateRequest().apply {
        this.groupName = groupName
        this.name = name
        this.engine = engine
        this.content = content
        this.description = description
        this.message = message
        this.tags = tags
        this.metadata = metadata
    }

    private fun updateRequest(
        name: String,
        engine: PromptEngine?,
        content: String,
        description: String? = null,
        groupName: String = "chat",
        message: String? = "Revise prompt",
        tags: List<String>? = null,
        metadata: PromptTemplateMetadata? = null,
    ) = PromptTemplateUpdateRequest().apply {
        this.groupName = groupName
        this.name = name
        this.engine = engine
        this.content = content
        this.description = description
        this.message = message
        this.tags = tags
        this.metadata = metadata
    }

    private fun previewRequest(
        engine: PromptEngine?,
        content: String,
        variables: Map<String, Any?>,
    ) = PromptTemplatePreviewRequest().apply {
        this.engine = engine
        this.content = content
        this.variables = variables
    }

    private fun renderRequest(variables: Map<String, Any?>) = PromptTemplateRenderRequest().apply {
        this.variables = variables
    }

    private fun sampleResponse(id: Long, name: String) = PromptTemplateResponse(
        id = id,
        groupName = "chat",
        name = name,
        version = PromptTemplateVersions.LATEST,
        revision = 1,
        engine = PromptEngine.SIMPLE,
        content = "Hello {{ name }}",
        description = "hi",
        tags = emptyList(),
        metadata = defaultMetadata(),
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-01-02T00:00:00Z"),
    )

    private fun stored(name: String, id: Long = 10L) = PromptTemplateEntity(
        id = id,
        ownerUserId = "ada",
        groupName = "chat",
        name = name,
        version = PromptTemplateVersions.LATEST,
        headRevision = 1,
        engine = PromptEngine.SIMPLE,
        content = "Hello {{ name }}",
        description = "hi",
        metadata = defaultMetadata(),
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-01-02T00:00:00Z"),
    )

    private fun stamp(entity: PromptTemplateEntity): PromptTemplateEntity {
        if (entity.id == null) entity.id = 11L
        entity.createdAt = Instant.parse("2026-01-01T00:00:00Z")
        entity.updatedAt = Instant.parse("2026-01-02T00:00:00Z")
        return entity
    }

    private fun defaultMetadata() = PromptTemplateMetadata().apply { color = WorkspaceColors.DEFAULT }

    private class PassThroughTransactionManager : PlatformTransactionManager {
        override fun getTransaction(definition: TransactionDefinition?): TransactionStatus = SimpleTransactionStatus()

        override fun commit(status: TransactionStatus) = Unit

        override fun rollback(status: TransactionStatus) = Unit
    }
}
