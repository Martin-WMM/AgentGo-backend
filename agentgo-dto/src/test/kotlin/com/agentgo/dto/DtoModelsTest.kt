package com.agentgo.dto

import com.agentgo.dto.auth.ProfileResponse
import com.agentgo.dto.auth.ProfileUpdateRequest
import com.agentgo.dto.auth.SessionResponse
import com.agentgo.dto.file.FileMetadataResponse
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DtoModelsTest {
    @Test
    fun storesProfileResponseFields() {
        val response = ProfileResponse("ada", "Ada Lovelace", "ada@example.com", null)

        assertEquals("ada", response.username)
        assertEquals("Ada Lovelace", response.displayName)
        assertEquals("ada@example.com", response.email)
        assertNull(response.avatarUrl)
    }

    @Test
    fun storesProfileUpdateFields() {
        val request = ProfileUpdateRequest("Ada Lovelace", null, "https://example.com/avatar.png")

        assertEquals("Ada Lovelace", request.displayName)
        assertNull(request.email)
        assertEquals("https://example.com/avatar.png", request.avatarUrl)
    }

    @Test
    fun supportsUnauthenticatedAndAuthenticatedSessions() {
        val anonymous = SessionResponse(false)
        val authenticated = SessionResponse(true, "user-123", "Ada", "ada@example.com", "avatar")

        assertEquals(false, anonymous.authenticated)
        assertNull(anonymous.subject)
        assertEquals(true, authenticated.authenticated)
        assertEquals("user-123", authenticated.subject)
        assertEquals("Ada", authenticated.name)
        assertEquals("ada@example.com", authenticated.email)
        assertEquals("avatar", authenticated.picture)
    }

    @Test
    fun storesFileMetadataAndDefaults() {
        val createdAt = Instant.parse("2026-01-01T00:00:00Z")
        val updatedAt = Instant.parse("2026-01-02T00:00:00Z")
        val id = UUID.randomUUID()
        val response = FileMetadataResponse(
            id = id,
            bucketName = "agentgo-bucket",
            objectKey = "workspace/owner/notes.md",
            contentType = "text/markdown",
            sizeBytes = 1024,
            createdAt = createdAt,
            updatedAt = updatedAt,
            downloadUrl = "https://example.com/file",
        )

        assertEquals(id, response.id)
        assertEquals("agentgo-bucket", response.bucketName)
        assertEquals("workspace/owner/notes.md", response.objectKey)
        assertEquals("text/markdown", response.contentType)
        assertEquals(1024, response.sizeBytes)
        assertNull(response.etag)
        assertNull(response.versionId)
        assertEquals(emptyMap(), response.metadata)
        assertEquals(createdAt, response.createdAt)
        assertEquals(updatedAt, response.updatedAt)
        assertEquals("https://example.com/file", response.downloadUrl)
    }
}
