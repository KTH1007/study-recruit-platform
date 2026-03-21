package com.study.platform.domain.apply.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplyRepository extends JpaRepository<Apply, UUID> {

    boolean existsByPostIdAndApplicantId(UUID postId, UUID applicantId);

    @Query("SELECT a FROM Apply a JOIN FETCH a.applicant WHERE a.post.id = :postId")
    List<Apply> findAllByPostIdWithApplicant(@Param("postId") UUID postId);

    @Query("SELECT a FROM Apply a JOIN FETCH a.post JOIN FETCH a.applicant WHERE a.id = :applyId")
    Optional<Apply> findByIdWithPostAndApplicant(@Param("applyId") UUID applyId);

    Optional<Apply> findByPostIdAndApplicantId(UUID postId, UUID applicantId);
}