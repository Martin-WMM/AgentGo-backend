package com.agentgo.file.mapper

import com.agentgo.dto.workspace.WorkspaceColors
import com.agentgo.dto.workspace.WorkspaceCreateRequest
import com.agentgo.dto.workspace.WorkspaceMetadata
import com.agentgo.dto.workspace.WorkspaceUpdateRequest
import com.agentgo.file.entity.WorkspaceEntity
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WorkspaceMapperImplTest {
    private val mapper = WorkspaceMapperImpl()

    @Test
    fun `create stores the default accent when metadata is omitted`() {
        val entity = mapper.toEntity(
            "ada",
            WorkspaceCreateRequest().apply {
                name = " Reports "
                description = "  drafts  "
            },
        )

        assertEquals("ada", entity.ownerUserId)
        assertEquals("Reports", entity.name)
        assertEquals("drafts", entity.description)
        assertEquals(WorkspaceColors.DEFAULT, entity.metadata.color)
    }

    @Test
    fun `create canonicalizes a palette color`() {
        val entity = mapper.toEntity(
            "ada",
            WorkspaceCreateRequest().apply {
                name = "Reports"
                metadata = WorkspaceMetadata().apply { color = "#b91c1c" }
            },
        )

        assertEquals("#B91C1C", entity.metadata.color)
    }

    @Test
    fun `update replaces the accent and keeps the previous accent when metadata is omitted`() {
        val entity = WorkspaceEntity(
            ownerUserId = "ada",
            name = "Reports",
            metadata = WorkspaceMetadata().apply { color = "#15803D" },
        )

        mapper.applyUpdate(
            entity,
            WorkspaceUpdateRequest().apply { name = "Archive" },
        )
        assertEquals("Archive", entity.name)
        assertEquals("#15803D", entity.metadata.color)

        mapper.applyUpdate(
            entity,
            WorkspaceUpdateRequest().apply {
                name = "Archive"
                metadata = WorkspaceMetadata().apply { color = "#6D28D9" }
            },
        )
        assertEquals("#6D28D9", entity.metadata.color)
    }

    @Test
    fun `response fills the default accent when the stored color is missing`() {
        val response = mapper.toResponse(
            WorkspaceEntity(
                id = UUID.randomUUID(),
                ownerUserId = "ada",
                name = "Reports",
                metadata = WorkspaceMetadata(),
                createdAt = Instant.parse("2026-01-01T00:00:00Z"),
                updatedAt = Instant.parse("2026-01-02T00:00:00Z"),
            ),
        )

        assertEquals(WorkspaceColors.DEFAULT, response.metadata.color)
    }

    @Test
    fun `create rejects a color outside the palette`() {
        assertFailsWith<IllegalArgumentException> {
            mapper.toEntity(
                "ada",
                WorkspaceCreateRequest().apply {
                    name = "Reports"
                    metadata = WorkspaceMetadata().apply { color = "#000000" }
                },
            )
        }
    }
}
