package com.study.platform.domain.post.model;

import com.study.platform.domain.user.model.User;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class StudyPostTest {

    private User author;
    private StudyPost post;

    @BeforeEach
    void setUp() {
        author = User.create("kakao-1", "작성자", "author@test.com");
        ReflectionTestUtils.setField(author, "id", UUID.randomUUID());
        post = StudyPost.create(author, "스터디 모집", "열심히 합니다", "Java", 5,
                LocalDateTime.now().plusDays(7));
    }

    @Test
    void validateOpen_OPEN상태_예외없음() {
        assertThatNoException().isThrownBy(post::validateOpen);
    }

    @Test
    void validateOpen_CLOSED상태_예외발생() {
        // given
        post.close();

        // when & then
        assertThatThrownBy(post::validateOpen)
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.POST_CLOSED));
    }

    @Test
    void validateOpen_FULL상태_예외발생() {
        // given
        post.markFull();

        // when & then
        assertThatThrownBy(post::validateOpen)
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.POST_CLOSED));
    }

    @Test
    void validateAuthor_작성자_예외없음() {
        UUID authorId = author.getId();
        assertThatNoException().isThrownBy(() -> post.validateAuthor(authorId));
    }

    @Test
    void validateAuthor_작성자아님_예외발생() {
        assertThatThrownBy(() -> post.validateAuthor(UUID.randomUUID()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void validateNotAuthor_작성자아님_예외없음() {
        assertThatNoException().isThrownBy(() -> post.validateNotAuthor(UUID.randomUUID()));
    }

    @Test
    void validateNotAuthor_작성자_예외발생() {
        UUID authorId = author.getId();
        assertThatThrownBy(() -> post.validateNotAuthor(authorId))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CANNOT_APPLY_OWN_POST));
    }

}