package com.study.platform.support.fake;

import com.study.platform.domain.comment.model.Comment;
import com.study.platform.domain.comment.model.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeCommentRepository implements CommentRepository {

    private final Map<UUID, Comment> store = new HashMap<>();

    @Override
    public Comment save(Comment comment) {
        if (comment.getId() == null) {
            ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
        }
        store.put(comment.getId(), comment);
        return comment;
    }

    @Override
    public void delete(Comment comment) {
        store.remove(comment.getId());
    }

    @Override
    public Page<Comment> findAllByPostIdWithAuthor(UUID postId, Pageable pageable) {
        var list = store.values().stream()
                .filter(c -> c.getPost().getId().equals(postId))
                .toList();
        return new PageImpl<>(list, pageable, list.size());
    }

    @Override
    public Optional<Comment> findByIdWithAuthor(UUID commentId) {
        return Optional.ofNullable(store.get(commentId));
    }
}
