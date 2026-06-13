package com.study.platform.global.model;

import jakarta.persistence.Embeddable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Embeddable
public record TechStack(String value) {

    public static TechStack of(String value) {
        if (value == null || value.isBlank()) {
            return new TechStack(null);
        }
        String normalized = String.join(",",
                Arrays.stream(value.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList());
        return new TechStack(normalized.isEmpty() ? null : normalized);
    }

    public List<String> toList() {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
