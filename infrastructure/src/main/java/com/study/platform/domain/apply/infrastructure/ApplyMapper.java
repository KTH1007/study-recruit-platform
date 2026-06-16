package com.study.platform.domain.apply.infrastructure;

import com.study.platform.domain.apply.model.Apply;
import org.springframework.stereotype.Component;

@Component
public class ApplyMapper {

    public ApplyJpaEntity toEntity(Apply apply) {
        return ApplyJpaEntity.builder()
                .id(apply.getId())
                .post(apply.getPost())
                .applicant(apply.getApplicant())
                .message(apply.getMessage())
                .status(apply.getStatus())
                .build();
    }

    public Apply toDomain(ApplyJpaEntity entity) {
        return Apply.reconstitute(
                entity.getId(),
                entity.getPost(),
                entity.getApplicant(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
