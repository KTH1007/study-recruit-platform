package com.study.platform.domain.post.model;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudyPostRepository extends JpaRepository<StudyPost, UUID> {

    @Query(value = "SELECT p FROM StudyPost p JOIN FETCH p.author " +
            "WHERE (:techStack IS NULL OR p.techStack LIKE %:techStack%) " +
            "AND (:status IS NULL OR p.status = :status)",
            countQuery = "SELECT COUNT(p) FROM StudyPost p " +
                    "WHERE (:techStack IS NULL OR p.techStack LIKE %:techStack%) " +
                    "AND (:status IS NULL OR p.status = :status)")
    Page<StudyPost> findAllWithFilter(
            @Param("techStack") String techStack,
            @Param("status") StudyPostStatus status,
            Pageable pageable
    );

    @Query("SELECT p FROM StudyPost p JOIN FETCH p.author WHERE p.id = :postId")
    Optional<StudyPost> findByIdWithAuthor(@Param("postId") UUID postId);

    @Query("SELECT p FROM StudyPost p WHERE p.status = :status AND p.deadline < :now")
    List<StudyPost> findExpiredPosts(@Param("now")LocalDateTime now, @Param("status") StudyPostStatus status);

    @Query("SELECT p FROM StudyPost p JOIN FETCH p.author WHERE p.status = :status AND p.deadline BETWEEN :start AND :end")
    List<StudyPost> findDeadlineReminderPosts(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("status") StudyPostStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM StudyPost p JOIN FETCH p.author WHERE p.id = :postId")
    Optional<StudyPost> findByIdWithAuthorForUpdate(@Param("postId") UUID postId);
}
