package com.study.platform.domain.post.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudyPostRepository {

    StudyPost save(StudyPost post);
    List<StudyPost> saveAll(List<StudyPost> posts);
    void delete(StudyPost post);
    Optional<StudyPost> findById(UUID id);
    Optional<StudyPost> findByIdWithAuthor(UUID postId);
    List<StudyPost> findExpiredPosts(LocalDateTime now, StudyPostStatus status);
    List<StudyPost> findDeadlineReminderPosts(LocalDateTime start, LocalDateTime end, StudyPostStatus status);
    Optional<StudyPost> findByIdWithAuthorForUpdate(UUID postId);
}
