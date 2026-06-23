package com.study.platform.domain.apply.dto.response

import com.study.platform.domain.apply.model.Apply
import com.study.platform.domain.apply.model.ApplyStatus
import java.time.LocalDateTime
import java.util.UUID

data class ApplyResponse(
    val id: UUID,
    val applicantNickname: String,
    val applicantTechStack: String?,
    val message: String,
    val status: ApplyStatus,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(apply: Apply): ApplyResponse = ApplyResponse(
            id = apply.id,
            applicantNickname = apply.applicant.nickname,
            applicantTechStack = apply.applicant.techStack,
            message = apply.message,
            status = apply.status,
            createdAt = apply.createdAt
        )
    }
}
