package com.study.platform.domain.comment.model;

import com.study.platform.domain.user.model.User;
import com.study.platform.global.event.DomainEventPublisher;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    private User author;
    private Comment comment;

    @BeforeEach
    void setUp() {
        author = User.create("kakao-1", "작성자", "author@test.com");
        ReflectionTestUtils.setField(author, "id", UUID.randomUUID());

        comment = Comment.create(null, author, "댓글 내용", e -> {});
    }

    @Test
    void validateAuthor_작성자_예외없음() {
        assertThatNoException().isThrownBy(() -> comment.validateAuthor(author.getId()));
    }

    @Test
    void validateAuthor_작성자아님_예외발생() {
        assertThatThrownBy(() -> comment.validateAuthor(UUID.randomUUID()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_COMMENT_AUTHOR));
    }
}