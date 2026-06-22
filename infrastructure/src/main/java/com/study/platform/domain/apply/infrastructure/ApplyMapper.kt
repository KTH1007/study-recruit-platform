package com.study.platform.domain.apply.infrastructure

import com.study.platform.domain.apply.model.Apply
import org.springframework.stereotype.Component

@Component
class ApplyMapper {

    fun toEntity(apply: Apply): ApplyJpaEntity {
        val entity = ApplyJpaEntity(
            id = apply.id,
            post = apply.post,
            applicant = apply.applicant,
            message = apply.message,
            status = apply.status
        )
        if (apply.createdAt != null) {
            entity.restoreTimestamps(apply.createdAt, apply.updatedAt)
        }
        return entity
    }

    fun toDomain(entity: ApplyJpaEntity): Apply = Apply.reconstitute(
        id = entity.id,
        post = entity.post,
        applicant = entity.applicant,
        message = entity.message,
        status = entity.status,
        createdAt = entity.createdAt!!,
        updatedAt = entity.updatedAt
    )
}
