package com.study.platform.domain.post.infrastructure;

import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudyPostJpaRepository extends JpaRepository<StudyPost, UUID> {

    @Query("SELECT p FROM StudyPost p JOIN FETCH p.author WHERE p.id = :postId")
    Optional<StudyPost> findByIdWithAuthor(@Param("postId") UUID postId);

    @Query("SELECT p FROM StudyPost p WHERE p.status = :status AND p.deadline < :now")
    List<StudyPost> findExpiredPosts(@Param("now") LocalDateTime now,
                                     @Param("status") StudyPostStatus status);

    @Query("SELECT p FROM StudyPost p JOIN FETCH p.author WHERE p.status = :status AND p.deadline BETWEEN :start AND :end")
    List<StudyPost> findDeadlineReminderPosts(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end,
                                              @Param("status") StudyPostStatus status);

    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM StudyPost p JOIN FETCH p.author WHERE p.id = :postId")
    Optional<StudyPost> findByIdWithAuthorForUpdate(@Param("postId") UUID postId);
}
