package com.study.platform.domain.post.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudyPostRepository {

    StudyPost save(StudyPost post);
    List<StudyPost> saveAll(List<StudyPost> posts);
    void delete(StudyPost post);
    Optional<StudyPost> findById(UUID id);

    Page<StudyPost> findAllWithFilter(String techStack, StudyPostStatus status, Pageable pageable);
    Optional<StudyPost> findByIdWithAuthor(UUID postId);
    List<StudyPost> findExpiredPosts(LocalDateTime now, StudyPostStatus status);
    List<StudyPost> findDeadlineReminderPosts(LocalDateTime start, LocalDateTime end, StudyPostStatus status);
    Optional<StudyPost> findByIdWithAuthorForUpdate(UUID postId);
}
