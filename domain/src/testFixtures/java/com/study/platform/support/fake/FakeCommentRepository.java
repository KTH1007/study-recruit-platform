package com.study.platform.support.fake;

import com.study.platform.domain.comment.model.Comment;
import com.study.platform.domain.comment.model.CommentRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
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
    public Optional<Comment> findByIdWithAuthor(UUID commentId) {
        return Optional.ofNullable(store.get(commentId));
    }

    public List<Comment> findAllByPostId(UUID postId) {
        return store.values().stream()
                .filter(c -> c.getPost().getId().equals(postId))
                .toList();
    }
}
