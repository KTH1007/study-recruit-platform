package com.study.platform.support.fake;

import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.post.model.StudyPostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeStudyPostRepository implements StudyPostRepository {

    private final Map<UUID, StudyPost> store = new HashMap<>();

    @Override
    public StudyPost save(StudyPost post) {
        if (post.getId() == null) {
            ReflectionTestUtils.setField(post, "id", UUID.randomUUID());
        }
        store.put(post.getId(), post);
        return post;
    }

    @Override
    public List<StudyPost> saveAll(List<StudyPost> posts) {
        posts.forEach(this::save);
        return posts;
    }

    @Override
    public void delete(StudyPost post) {
        store.remove(post.getId());
    }

    @Override
    public Optional<StudyPost> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<StudyPost> findByIdWithAuthor(UUID postId) {
        return Optional.ofNullable(store.get(postId));
    }

    @Override
    public Optional<StudyPost> findByIdWithAuthorForUpdate(UUID postId) {
        return Optional.ofNullable(store.get(postId));
    }

    @Override
    public Page<StudyPost> findAllWithFilter(String techStack, StudyPostStatus status, Pageable pageable) {
        throw new UnsupportedOperationException("필요 시 구현");
    }

    @Override
    public List<StudyPost> findExpiredPosts(LocalDateTime now, StudyPostStatus status) {
        throw new UnsupportedOperationException("필요 시 구현");
    }

    @Override
    public List<StudyPost> findDeadlineReminderPosts(LocalDateTime start, LocalDateTime end, StudyPostStatus status) {
        throw new UnsupportedOperationException("필요 시 구현");
    }
}
