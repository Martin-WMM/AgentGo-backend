package com.agentgo.dto.presentation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/** Rejects an unusable [ResourceIcon] while allowing a missing icon. */
class ResourceIconValidator : ConstraintValidator<ValidResourceIcon, ResourceIcon?> {
    override fun isValid(value: ResourceIcon?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        return try {
            ResourceIcons.canonicalize(value)
            true
        } catch (_: IllegalArgumentException) {
            false
        }
    }
}
