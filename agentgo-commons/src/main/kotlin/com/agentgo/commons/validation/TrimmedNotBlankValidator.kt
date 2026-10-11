package com.agentgo.commons.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/**
 * Hibernate Validator implementation of [TrimmedNotBlank].
 *
 * A value is valid when it is non-null and its trimmed form is non-empty.
 */
class TrimmedNotBlankValidator : ConstraintValidator<TrimmedNotBlank, CharSequence?> {
    override fun isValid(value: CharSequence?, context: ConstraintValidatorContext): Boolean =
        value != null && value.toString().trim().isNotEmpty()
}
