package com.agentgo.events.service.impl

import com.agentgo.commons.dto.http.request.PageRequest
import com.agentgo.commons.dto.http.request.SortOrder
import com.agentgo.dto.events.SystemEventCreateRequest
import com.agentgo.dto.events.SystemEventUpdateRequest
import com.agentgo.events.entity.SystemEventEntity
import com.agentgo.events.exception.InvalidSystemEventRequestException
import com.agentgo.events.exception.SystemEventException
import com.agentgo.events.exception.SystemEventNotFoundException
import com.agentgo.events.mapper.SystemEventMapperImpl
import com.agentgo.events.repository.SystemEventRepository
import com.agentgo.events.security.AuthenticatedUserIdResolver
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus

class SystemEventServiceImplTest {
    private val now = Instant.parse("2026-10-26T12:00:00Z")
    private val repository = mock<SystemEventRepository>()
    private val resolver = mock<AuthenticatedUserIdResolver>()
    private val service = SystemEventServiceImpl(
        repository,
        SystemEventMapperImpl(),
        resolver,
        Clock.fixed(now, ZoneOffset.UTC),
        PassThroughTransactionManager(),
    )

    @Test
    fun `create stores the caller when the operator is blank and defaults the time`() {
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.save(any<SystemEventEntity>())).thenAnswer { stamp(it.arguments[0] as SystemEventEntity) }
        whenever(repository.count()).thenReturn(1)

        val response = service.create(
            SystemEventCreateRequest().apply {
                name = "  Model created  "
                domain = " models "
                `operator` = "   "
                comment = "  hello  "
            },
        )

        assertEquals("Model created", response.name)
        assertEquals("models", response.domain)
        assertEquals("ada", response.`operator`)
        assertEquals("hello", response.comment)
        assertEquals(now, response.occurredAt)
        verify(repository).deleteOlderThan(Instant.parse("2026-07-26T12:00:00Z"))
    }

    @Test
    fun `create rejects an event older than three months before saving`() {
        whenever(resolver.requireUserId()).thenReturn("ada")

        val exception = assertFailsWith<InvalidSystemEventRequestException> {
            service.create(
                SystemEventCreateRequest().apply {
                    name = "Old import"
                    domain = "files"
                    occurredAt = Instant.parse("2026-01-01T00:00:00Z")
                },
            )
        }

        assertEquals(9702, exception.code)
        verify(repository, never()).save(any<SystemEventEntity>())
    }

    @Test
    fun `create keeps an event that lands exactly on the retention cutoff`() {
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.save(any<SystemEventEntity>())).thenAnswer { stamp(it.arguments[0] as SystemEventEntity) }
        whenever(repository.count()).thenReturn(1)

        val response = service.create(
            SystemEventCreateRequest().apply {
                name = "Boundary"
                domain = "files"
                occurredAt = Instant.parse("2026-07-26T12:00:00Z")
            },
        )

        assertEquals(Instant.parse("2026-07-26T12:00:00Z"), response.occurredAt)
    }

    @Test
    fun `create rejects an event outside the newest one thousand`() {
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.save(any<SystemEventEntity>())).thenAnswer { stamp(it.arguments[0] as SystemEventEntity) }
        whenever(repository.countNewerThan(any(), any())).thenReturn(1000)

        assertFailsWith<InvalidSystemEventRequestException> {
            service.create(
                SystemEventCreateRequest().apply {
                    name = "Late arrival"
                    domain = "models"
                    occurredAt = now.minusSeconds(60)
                },
            )
        }

        verify(repository, never()).deleteOlderThan(any())
    }

    @Test
    fun `create rejects an unauthenticated caller`() {
        whenever(resolver.requireUserId()).thenThrow(unauthenticated())

        val exception = assertFailsWith<SystemEventException> {
            service.create(SystemEventCreateRequest().apply {
                name = "Model created"
                domain = "models"
            })
        }

        assertEquals("AUTH-002", exception.responseType)
        verify(repository, never()).save(any<SystemEventEntity>())
    }

    @Test
    fun `get returns the stored event`() {
        val entity = stored()
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.findById(entity.id!!)).thenReturn(Optional.of(entity))

        val response = service.get(entity.id!!)

        assertEquals("Model created", response.name)
        assertEquals("ada", response.`operator`)
    }

    @Test
    fun `get rejects an unknown id`() {
        val id = 1L
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.findById(id)).thenReturn(Optional.empty())

        assertFailsWith<SystemEventNotFoundException> { service.get(id) }
    }

    @Test
    fun `list defaults to occurredAt descending and reports the requested page`() {
        val entity = stored()
        whenever(resolver.requireUserId()).thenReturn("ada")
        whenever(repository.findAll(any<Pageable>())).thenReturn(PageImpl(listOf(entity)))

        val listed = service.list(PageRequest(page = 2, size = 20), "  ", null)

        assertEquals(1, listed.data.size)
        assertEquals(2, listed.number)
        assertEquals(20, listed.pageSize)
        val pageable = argumentCaptor<Pageable>()
        verify(repository).findAll(pageable.capture())
        assertEquals(1, pageable.firstValue.pageNumber)
        assertEquals(Sort.by(Sort.Direction.DESC, "occurredAt"), pageable.firstValue.sort)
    }

    @Test
    fun `list filters by domain and operator and sorts the operator ascending`() {
        whenever(resolver.requireUserId()).thenReturn("ada")
        val specification = argumentCaptor<Specification<SystemEventEntity>>()
        whenever(repository.findAll(specification.capture(), any<Pageable>()))
            .thenReturn(PageImpl(emptyList()))

        val listed = service.list(
            PageRequest(sort = listOf("operator"), order = SortOrder.ASC),
            "models",
            "ada",
        )

        assertEquals(0, listed.totalElements)
        assertEquals(true, listed.empty)
        val pageable = argumentCaptor<Pageable>()
        verify(repository).findAll(any<Specification<SystemEventEntity>>(), pageable.capture())
        assertEquals(Sort.by(Sort.Direction.ASC, "operatorName"), pageable.firstValue.sort)

        val root = mock<jakarta.persistence.criteria.Root<SystemEventEntity>>()
        val query = mock<jakarta.persistence.criteria.CriteriaQuery<*>>()
        val builder = mock<jakarta.persistence.criteria.CriteriaBuilder>()
        val domainPath = mock<jakarta.persistence.criteria.Path<String>>()
        val operatorPath = mock<jakarta.persistence.criteria.Path<String>>()
        val domainPredicate = mock<jakarta.persistence.criteria.Predicate>()
        val operatorPredicate = mock<jakarta.persistence.criteria.Predicate>()
        whenever(root.get<String>("domain")).thenReturn(domainPath)
        whenever(root.get<String>("operatorName")).thenReturn(operatorPath)
        val combined = mock<jakarta.persistence.criteria.Predicate>()
        whenever(builder.equal(domainPath, "models")).thenReturn(domainPredicate)
        whenever(builder.equal(operatorPath, "ada")).thenReturn(operatorPredicate)
        whenever(builder.and(domainPredicate, operatorPredicate)).thenReturn(combined)
        specification.firstValue.toPredicate(root, query, builder)
        verify(builder).equal(domainPath, "models")
        verify(builder).equal(operatorPath, "ada")
        verify(builder).and(domainPredicate, operatorPredicate)
    }

    @Test
    fun `list applies a domain-only filter specification`() {
        whenever(resolver.requireUserId()).thenReturn("ada")
        val specification = argumentCaptor<Specification<SystemEventEntity>>()
        whenever(repository.findAll(specification.capture(), any<Pageable>()))
            .thenReturn(PageImpl(emptyList()))

        service.list(PageRequest(), "models", "  ")

        val root = mock<jakarta.persistence.criteria.Root<SystemEventEntity>>()
        val query = mock<jakarta.persistence.criteria.CriteriaQuery<*>>()
        val builder = mock<jakarta.persistence.criteria.CriteriaBuilder>()
        val domainPath = mock<jakarta.persistence.criteria.Path<String>>()
        val domainPredicate = mock<jakarta.persistence.criteria.Predicate>()
        val combined = mock<jakarta.persistence.criteria.Predicate>()
        whenever(root.get<String>("domain")).thenReturn(domainPath)
        whenever(builder.equal(domainPath, "models")).thenReturn(domainPredicate)
        whenever(builder.and(domainPredicate)).thenReturn(combined)
        specification.firstValue.toPredicate(root, query, builder)
        verify(builder).equal(domainPath, "models")
        verify(builder).and(domainPredicate)
    }

