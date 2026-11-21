package com.agentgo.events.mapper

import com.agentgo.dto.events.SystemEventCreateRequest
import com.agentgo.dto.events.SystemEventUpdateRequest
import com.agentgo.events.entity.SystemEventEntity
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SystemEventMapperImplTest {
    private val mapper = SystemEventMapperImpl()
    private val now = Instant.parse("2026-10-26T12:00:00Z")

    @Test
    fun `toEntity trims fields and fills a blank operator and omitted time`() {
        val entity = mapper.toEntity(
            "ada",
            SystemEventCreateRequest().apply {
                name = "  Model created  "
                domain = " models "
                `operator` = "  "
                comment = "   "
            },
            now,
        )

        assertEquals("Model created", entity.name)
        assertEquals("models", entity.domain)
        assertEquals("ada", entity.operatorName)
        assertNull(entity.comment)
        assertEquals(now, entity.occurredAt)
    }

    @Test
    fun `applyUpdate keeps the stored time when the request omits it`() {
        val entity = SystemEventEntity(
            id = 1L,
            name = "Model created",
            domain = "models",
            operatorName = "ada",
            comment = "before",
            occurredAt = now,
            createdAt = now,
            updatedAt = now,
        )

        mapper.applyUpdate(
            entity,
            "grace",
            SystemEventUpdateRequest().apply {
                name = "Model renamed"
                domain = "files"
                comment = " after "
            },
        )

        assertEquals("Model renamed", entity.name)
        assertEquals("files", entity.domain)
        assertEquals("grace", entity.operatorName)
        assertEquals("after", entity.comment)
        assertEquals(now, entity.occurredAt)
    }

    @Test
    fun `toResponse requires a persisted id and audit timestamps`() {
        val entity = SystemEventEntity(name = "Model created", domain = "models", operatorName = "ada")

        assertFailsWith<IllegalArgumentException> { mapper.toResponse(entity) }
    }
}
