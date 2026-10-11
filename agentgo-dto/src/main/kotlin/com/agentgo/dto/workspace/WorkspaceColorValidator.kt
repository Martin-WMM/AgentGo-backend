package com.agentgo.dto.workspace

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/** Rejects blank or unknown workspace accent colors while allowing a missing color. */
class WorkspaceColorValidator : ConstraintValidator<WorkspaceColor, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        return WorkspaceColors.canonicalize(value) != null
    }
}
