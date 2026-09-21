package com.apexcare.auth.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    PATIENT,
    DOCTOR,
    ADMIN;

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Role fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(value.trim())) {
                return role;
            }
        }

        throw new IllegalArgumentException("Invalid role: " + value);
    }

    public String authority() {
        return "ROLE_" + name();
    }
}
