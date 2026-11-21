package com.agentgo.dto.presentation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

/**
 * Requires a resource icon to be null or a canonical Iconify id / http(s) URL.
 *
 * Null means the resource has no icon. Present values are checked with [ResourceIcons].
 */
@MustBeDocumented
@Target(FIELD, VALUE_PARAMETER, PROPERTY_GETTER)
@Retention(RUNTIME)
@Constraint(validatedBy = [ResourceIconValidator::class])
annotation class ValidResourceIcon(
    val message: String = "must be an Iconify id or an absolute http(s) URL",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
