package com.study.platform.global.model

import jakarta.persistence.Embeddable

@Embeddable
class TechStack(val value: String? = null) {

    companion object {
        fun of(value: String?): TechStack {
            if (value == null || value.isBlank()) return TechStack(null)
            val normalized = value.split(",").map { it.trim() }.filter { it.isNotEmpty() }.joinToString(",")
            return TechStack(normalized.ifEmpty { null })
        }
    }

    fun toList(): List<String> {
        if (value == null || value.isBlank()) return emptyList()
        return value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
