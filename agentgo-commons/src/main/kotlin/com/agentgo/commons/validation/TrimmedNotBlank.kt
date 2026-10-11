package com.agentgo.commons.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

/**
 * Validates that a character sequence is not null and not blank after trimming.
 *
 * Unlike [jakarta.validation.constraints.NotBlank], this constraint rejects values that
 * still contain leading or trailing whitespace so callers must send already-normalized text
 * or rely on mappers that trim before persistence.
 */
@MustBeDocumented
@Target(FIELD, VALUE_PARAMETER, ANNOTATION_CLASS, PROPERTY_GETTER)
@Retention(RUNTIME)
@Constraint(validatedBy = [TrimmedNotBlankValidator::class])
annotation class TrimmedNotBlank(
    val message: String = "must not be blank after trimming",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
