package com.agentgo.file.controller

import com.agentgo.dto.file.BatchPathsRequest
import com.agentgo.file.service.WorkspaceFileService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus

class FileControllerTest {
    private val fileService = mock<WorkspaceFileService>()
    private val controller = FileController(fileService)

    @Test
    fun `deleteWorkspaces returns http 204 and delegates paths`() {
        val request = BatchPathsRequest().apply { paths = listOf("1/notes.txt", "1/diagram.png") }

        val deleted = controller.deleteWorkspaces(request)

        assertEquals(HttpStatus.NO_CONTENT, deleted.statusCode)
        assertNull(deleted.body)
        verify(fileService).deleteWorkspaces(listOf("1/notes.txt", "1/diagram.png"))
    }

    @Test
    fun `deleteWorkspaces delegates an empty list when paths are absent`() {
        val deleted = controller.deleteWorkspaces(BatchPathsRequest())

        assertEquals(HttpStatus.NO_CONTENT, deleted.statusCode)
        verify(fileService).deleteWorkspaces(emptyList())
    }
}
