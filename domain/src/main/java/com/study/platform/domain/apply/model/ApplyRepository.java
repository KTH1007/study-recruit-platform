package com.study.platform.domain.apply.model;

import java.util.Optional;
import java.util.UUID;

public interface ApplyRepository {

    Apply save(Apply apply);
    void delete(Apply apply);
    Optional<Apply> findById(UUID id);

    boolean existsByPostIdAndApplicantId(UUID postId, UUID applicantId);
    Optional<Apply> findByIdWithPostAndApplicant(UUID applyId);
    Optional<Apply> findByPostIdAndApplicantId(UUID postId, UUID applicantId);
    long countByPostIdAndStatus(UUID postId, ApplyStatus status);
    Optional<Apply> findByIdWithPostAndApplicantForUpdate(UUID applyId);
}
