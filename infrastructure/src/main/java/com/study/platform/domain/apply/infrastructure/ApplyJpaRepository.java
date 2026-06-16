package com.study.platform.domain.apply.infrastructure;

import com.study.platform.domain.apply.model.Apply;
import com.study.platform.domain.apply.model.ApplyStatus;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ApplyJpaRepository extends JpaRepository<ApplyJpaEntity, UUID> {

    boolean existsByPostIdAndApplicantId(UUID postId, UUID applicantId);

    @Query("SELECT a " +
            "FROM ApplyJpaEntity a JOIN FETCH a.post " +
            "JOIN FETCH a.applicant " +
            "WHERE a.id = :applyId")
    Optional<ApplyJpaEntity> findByIdWithPostAndApplicant(@Param("applyId") UUID applyId);

    Optional<ApplyJpaEntity> findByPostIdAndApplicantId(UUID postId, UUID applicantId);

    long countByPostIdAndStatus(UUID postId, ApplyStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a " +
            "FROM ApplyJpaEntity a JOIN FETCH a.post " +
            "JOIN FETCH a.applicant " +
            "WHERE a.id = :applyId")
    Optional<ApplyJpaEntity> findByIdWithPostAndApplicantForUpdate(@Param("applyId") UUID applyId);
}
