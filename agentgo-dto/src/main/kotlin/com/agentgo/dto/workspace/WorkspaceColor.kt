package com.agentgo.dto.workspace

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

/**
 * Requires a workspace accent to be null or one of [WorkspaceColors.palette].
 *
 * Null means the caller omitted a color and the service may apply [WorkspaceColors.DEFAULT].
 */
@MustBeDocumented
@Target(FIELD, VALUE_PARAMETER, PROPERTY_GETTER)
@Retention(RUNTIME)
@Constraint(validatedBy = [WorkspaceColorValidator::class])
annotation class WorkspaceColor(
    val message: String = "must be a supported workspace color",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
