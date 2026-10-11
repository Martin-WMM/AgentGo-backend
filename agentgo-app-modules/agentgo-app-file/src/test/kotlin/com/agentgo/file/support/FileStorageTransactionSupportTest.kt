package com.agentgo.file.support

import com.agentgo.file.service.FileService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FileStorageTransactionSupportTest {
    private val fileService = mock<FileService>()
    private val support = FileStorageTransactionSupport(NoOpPlatformTransactionManager(), fileService)

    @Test
    fun `commitMetadataForStoredObject returns action result when write succeeds`() {
        val result = support.commitMetadataForStoredObject("workspace/a.txt") { "ok" }

        assertEquals("ok", result)
        verify(fileService, never()).deleteObject("workspace/a.txt")
    }

    @Test
    fun `commitMetadataForStoredObject deletes object when metadata write fails`() {
        val failure = IllegalStateException("db down")

        val thrown = assertFailsWith<IllegalStateException> {
            support.commitMetadataForStoredObject("workspace/a.txt") {
                throw failure
            }
        }

        assertEquals(failure, thrown)
        verify(fileService).deleteObject("workspace/a.txt")
    }

    @Test
    fun `commitMetadataForStoredObject skips compensation when overwrite metadata write fails`() {
        val failure = IllegalStateException("db down")

        val thrown = assertFailsWith<IllegalStateException> {
            support.commitMetadataForStoredObject("Avatars/user.png", compensateOnFailure = false) {
                throw failure
            }
        }

        assertEquals(failure, thrown)
        verify(fileService, never()).deleteObject("Avatars/user.png")
    }

    @Test
    fun `commitMetadataForStoredObject suppresses compensation failure on original exception`() {
        whenever(fileService.deleteObject("workspace/a.txt")).thenThrow(IllegalStateException("minio down"))

        val thrown = assertFailsWith<IllegalStateException> {
            support.commitMetadataForStoredObject("workspace/a.txt") {
                throw IllegalStateException("db down")
            }
        }

        assertEquals("db down", thrown.message)
        assertTrue(thrown.suppressed.any { it.message == "minio down" })
    }

    @Test
    fun `deleteObjectAfterCommit removes object only after write commit`() {
        support.write {
            support.deleteObjectAfterCommit("workspace/a.txt")
            verify(fileService, never()).deleteObject("workspace/a.txt")
        }

        verify(fileService).deleteObject("workspace/a.txt")
    }

    @Test
    fun `deleteObjectAfterCommit skips deletion when write rolls back`() {
        assertFailsWith<IllegalStateException> {
            support.write {
                support.deleteObjectAfterCommit("workspace/a.txt")
                throw IllegalStateException("rollback")
            }
        }

        verify(fileService, never()).deleteObject("workspace/a.txt")
    }
}
