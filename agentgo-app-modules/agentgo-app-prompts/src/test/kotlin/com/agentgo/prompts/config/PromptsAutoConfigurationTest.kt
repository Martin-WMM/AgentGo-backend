package com.agentgo.prompts.config

import com.agentgo.commons.dto.http.response.status.error.ExceptionResponseCodeRegistry
import com.agentgo.prompts.exception.DuplicatePromptTemplateNameException
import com.agentgo.prompts.exception.InvalidPromptTemplateRequestException
import com.agentgo.prompts.exception.PromptTemplateNotFoundException
import com.agentgo.prompts.exception.PromptTemplateRenderException
import com.agentgo.prompts.exception.UnauthorizedPromptTemplateException
import kotlin.test.Test
import kotlin.test.assertEquals

class PromptsAutoConfigurationTest {
    @Test
    fun `registers prompt template exception response codes`() {
        PromptsAutoConfiguration()

        assertEquals(9001, ExceptionResponseCodeRegistry.responseCode(PromptTemplateNotFoundException::class)?.code)
        assertEquals(
            "PROMPT-002",
            ExceptionResponseCodeRegistry.responseCode(InvalidPromptTemplateRequestException::class)?.responseType,
        )
        assertEquals(9003, ExceptionResponseCodeRegistry.responseCode(DuplicatePromptTemplateNameException::class)?.code)
        assertEquals(9004, ExceptionResponseCodeRegistry.responseCode(PromptTemplateRenderException::class)?.code)
        assertEquals(
            "PROMPT-005",
            ExceptionResponseCodeRegistry.responseCode(UnauthorizedPromptTemplateException::class)?.responseType,
        )
    }
}
