package com.study.platform.domain.post.infrastructure;

import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.post.model.StudyPostStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StudyPostRepositoryAdapter implements StudyPostRepository {

    private final StudyPostJpaRepository studyPostJpaRepository;

    @Override
    public StudyPost save(StudyPost post) {
        return studyPostJpaRepository.saveAndFlush(post);
    }

    @Override
    public List<StudyPost> saveAll(List<StudyPost> posts) {
        return studyPostJpaRepository.saveAll(posts);
    }

    @Override
    public void delete(StudyPost post) {
        studyPostJpaRepository.delete(post);
    }

    @Override
    public Optional<StudyPost> findById(UUID id) {
        return studyPostJpaRepository.findById(id);
    }

    @Override
    public Optional<StudyPost> findByIdWithAuthor(UUID postId) {
        return studyPostJpaRepository.findByIdWithAuthor(postId);
    }

    @Override
    public List<StudyPost> findExpiredPosts(LocalDateTime now, StudyPostStatus status) {
        return studyPostJpaRepository.findExpiredPosts(now, status);
    }

    @Override
    public List<StudyPost> findDeadlineReminderPosts(LocalDateTime start, LocalDateTime end, StudyPostStatus status) {
        return studyPostJpaRepository.findDeadlineReminderPosts(start, end, status);
    }

    @Override
    public Optional<StudyPost> findByIdWithAuthorForUpdate(UUID postId) {
        return studyPostJpaRepository.findByIdWithAuthorForUpdate(postId);
    }
}
