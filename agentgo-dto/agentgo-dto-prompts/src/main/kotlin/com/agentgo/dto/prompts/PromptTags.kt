package com.agentgo.dto.prompts

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.reflect.KClass

/**
 * Validates a prompt-template tag list.
 *
 * Null is accepted. Blank entries are ignored. Every remaining tag must be at most
 * [maxLength] characters and must not contain a comma or a control character.
 * At most [maxCount] tags remain after trimming.
 */
@MustBeDocumented
@Target(FIELD, PROPERTY_GETTER)
@Retention(RUNTIME)
@Constraint(validatedBy = [PromptTagsValidator::class])
annotation class PromptTags(
    val maxCount: Int = 20,
    val maxLength: Int = 32,
    val message: String = "must contain at most {maxCount} tags, each at most {maxLength} characters and without commas",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
