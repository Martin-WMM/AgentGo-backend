package com.agentgo.models.config

import com.agentgo.commons.dto.http.response.status.error.ExceptionResponseCodeRegistry
import com.agentgo.models.exception.DuplicateModelNameException
import com.agentgo.models.exception.InvalidModelRequestException
import com.agentgo.models.exception.ModelNotFoundException
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelsAutoConfigurationTest {
    @Test
    fun registersModelExceptionResponseCodes() {
        ModelsAutoConfiguration()

        assertEquals(8001, ExceptionResponseCodeRegistry.responseCode(ModelNotFoundException::class)?.code)
        assertEquals("MODEL-002", ExceptionResponseCodeRegistry.responseCode(InvalidModelRequestException::class)?.responseType)
        assertEquals(8003, ExceptionResponseCodeRegistry.responseCode(DuplicateModelNameException::class)?.code)
    }
}
