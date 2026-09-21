package com.apexcare.auth.dto.validation;

import com.apexcare.auth.model.Role;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PublicRoleValidator implements ConstraintValidator<PublicRole, Role> {

    @Override
    public boolean isValid(Role role, ConstraintValidatorContext context) {
        if (role == null) {
            return true;
        }
        return role == Role.PATIENT || role == Role.DOCTOR;
    }
}
