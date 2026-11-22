package com.agentgo.ai.tool

import com.agentgo.core.tool.ToolRuntimeContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.springframework.security.core.context.SecurityContextHolder

class BashToolSecuritySupportTest {
    @Test
    fun `restores owner principal from runtime metadata when security context is empty`() {
        SecurityContextHolder.clearContext()
        val context =
            ToolRuntimeContext(
                metadata = mapOf("ownerUserId" to "ada"),
            )

        val seen =
            BashToolSecuritySupport.withAuthenticatedContext(context) {
                SecurityContextHolder.getContext().authentication?.name
            }

        assertEquals("ada", seen)
        assertNull(SecurityContextHolder.getContext().authentication)
        SecurityContextHolder.clearContext()
    }
}
