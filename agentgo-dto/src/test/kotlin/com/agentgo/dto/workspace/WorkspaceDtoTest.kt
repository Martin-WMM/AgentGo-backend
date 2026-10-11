package com.agentgo.dto.workspace

import jakarta.validation.ConstraintValidatorContext
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkspaceDtoTest {
    private val validator = WorkspaceColorValidator()

    @Test
    fun `canonicalizes palette colors and rejects unknown values`() {
        assertEquals(WorkspaceColors.DEFAULT, WorkspaceColors.canonicalize("  #1d4ed8 "))
        assertEquals("#BE185D", WorkspaceColors.canonicalize("#be185d"))
        assertNull(WorkspaceColors.canonicalize("  "))
        assertNull(WorkspaceColors.canonicalize("#FFFFFF"))
        assertTrue(WorkspaceColors.palette.contains(WorkspaceColors.DEFAULT))
        assertEquals(10, WorkspaceColors.palette.size)
    }

    @Test
    fun `accepts a missing color and a known accent`() {
        assertTrue(validator.isValid(null, unusedContext()))
        assertTrue(validator.isValid("  #0e7490 ", unusedContext()))
        assertFalse(validator.isValid("not-a-color", unusedContext()))
    }

    @Test
    fun `stores workspace requests, metadata, and responses`() {
        val metadata = WorkspaceMetadata().apply { color = WorkspaceColors.DEFAULT }
        val create = WorkspaceCreateRequest().apply {
            name = "Reports"
            description = "Quarterly report drafts"
            this.metadata = metadata
        }
        val update = WorkspaceUpdateRequest().apply {
            name = "Reports"
            description = null
            this.metadata = null
        }
        val createdAt = Instant.parse("2026-01-01T00:00:00Z")
        val updatedAt = Instant.parse("2026-01-02T00:00:00Z")
        val id = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6")
        val response = WorkspaceResponse(
            id = id,
            name = "Reports",
            description = "Quarterly report drafts",
            metadata = metadata,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

        assertEquals("Reports", create.name)
        assertEquals("Quarterly report drafts", create.description)
        assertEquals(WorkspaceColors.DEFAULT, create.metadata?.color)
        assertEquals("Reports", update.name)
        assertNull(update.description)
        assertNull(update.metadata)
        assertEquals(id, response.id)
        assertEquals("Reports", response.name)
        assertEquals("Quarterly report drafts", response.description)
        assertEquals(metadata, response.metadata)
        assertEquals(createdAt, response.createdAt)
        assertEquals(updatedAt, response.updatedAt)
        assertEquals("WorkspaceMetadata(color=#1D4ED8)", metadata.toString())
        assertEquals(metadata, WorkspaceMetadata().apply { color = WorkspaceColors.DEFAULT })
        assertEquals(metadata.hashCode(), WorkspaceMetadata().apply { color = WorkspaceColors.DEFAULT }.hashCode())
        assertNotEquals(metadata, WorkspaceMetadata())
        assertFalse(metadata.equals("Reports"))
        assertEquals(0, WorkspaceMetadata().hashCode())
        assertEquals("WorkspaceMetadata(color=null)", WorkspaceMetadata().toString())
    }

    private fun unusedContext(): ConstraintValidatorContext = Proxy.newProxyInstance(
        javaClass.classLoader,
        arrayOf(ConstraintValidatorContext::class.java),
        InvocationHandler { _, _, _ -> null },
    ) as ConstraintValidatorContext
}
