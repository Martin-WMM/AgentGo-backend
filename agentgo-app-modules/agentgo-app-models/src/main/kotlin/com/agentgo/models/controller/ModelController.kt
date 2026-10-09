package com.agentgo.models.controller

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.success.ModelsResponseCodeRegistry
import com.agentgo.dto.models.ModelCreateRequest
import com.agentgo.dto.models.ModelResponse
import com.agentgo.dto.models.ModelType
import com.agentgo.dto.models.ModelUpdateRequest
import com.agentgo.models.controller.apidoc.ModelControllerApiDoc
import com.agentgo.models.service.ModelService
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * HTTP adapter for authenticated model CRUD operations.
 *
 * @property modelService user-scoped model application service
 */
@RestController
class ModelController(
    private val modelService: ModelService,
) : ModelControllerApiDoc {
    override fun create(request: ModelCreateRequest): CommonHttpResponse<ModelResponse> =
        CommonHttpResponse(
            code = ModelsResponseCodeRegistry.CREATED,
            responseType = ModelsResponseCodeRegistry.CREATED_TYPE,
            message = "Model created",
            data = modelService.create(request),
        )

    override fun get(id: UUID): CommonHttpResponse<ModelResponse> =
        CommonHttpResponse(
            code = ModelsResponseCodeRegistry.RETRIEVED,
            responseType = ModelsResponseCodeRegistry.RETRIEVED_TYPE,
            message = "Model retrieved",
            data = modelService.get(id),
        )

    override fun list(type: ModelType?): CommonHttpResponse<List<ModelResponse>> =
        CommonHttpResponse(
            code = ModelsResponseCodeRegistry.LIST_RETRIEVED,
            responseType = ModelsResponseCodeRegistry.LIST_RETRIEVED_TYPE,
            message = "Models listed",
            data = modelService.list(type),
        )

    override fun update(id: UUID, request: ModelUpdateRequest): CommonHttpResponse<ModelResponse> =
        CommonHttpResponse(
            code = ModelsResponseCodeRegistry.UPDATED,
            responseType = ModelsResponseCodeRegistry.UPDATED_TYPE,
            message = "Model updated",
            data = modelService.update(id, request),
        )

    override fun delete(id: UUID): ResponseEntity<Void> {
        modelService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
