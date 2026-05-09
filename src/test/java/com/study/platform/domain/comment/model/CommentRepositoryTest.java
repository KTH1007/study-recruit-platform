package com.study.platform.domain.comment.model;

import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.study.platform.global.support.AbstractIntegrationTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CommentRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private StudyPostRepository studyPostRepository;

    @Autowired
    private UserRepository userRepository;

    private User author;
    private StudyPost post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        author = userRepository.save(User.create("kakao-1", "작성자", "author@test.com"));
        post = studyPostRepository.save(StudyPost.create(
                author, "스터디 모집", "열심히 합니다", "Java", 5,
                LocalDateTime.now().plusDays(7)
        ));
        comment = commentRepository.save(Comment.create(post, author, "좋은 스터디네요"));
    }

    @Test
    void findAllByPostIdWithAuthor_성공() {
        // when
        Page<Comment> result = commentRepository.findAllByPostIdWithAuthor(post.getId(), PageRequest.of(0, 20));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("좋은 스터디네요");
        assertThat(result.getContent().get(0).getAuthor().getNickname()).isEqualTo("작성자");
    }

    @Test
    void findAllByPostIdWithAuthor_댓글없음_빈페이지() {
        // given
        StudyPost otherPost = studyPostRepository.save(StudyPost.create(
                author, "다른 스터디", "열심히 합니다", "Kotlin", 3,
                LocalDateTime.now().plusDays(7)
        ));

        // when
        Page<Comment> result = commentRepository.findAllByPostIdWithAuthor(otherPost.getId(), PageRequest.of(0, 20));

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void findAllByPostIdWithAuthor_페이징_성공() {
        // given
        commentRepository.save(Comment.create(post, author, "두 번째 댓글"));
        commentRepository.save(Comment.create(post, author, "세 번째 댓글"));

        // when
        Page<Comment> firstPage = commentRepository.findAllByPostIdWithAuthor(post.getId(), PageRequest.of(0, 2));
        Page<Comment> secondPage = commentRepository.findAllByPostIdWithAuthor(post.getId(), PageRequest.of(1, 2));

        // then
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
    }

    @Test
    void findByIdWithAuthor_성공() {
        // when
        Optional<Comment> result = commentRepository.findByIdWithAuthor(comment.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getContent()).isEqualTo("좋은 스터디네요");
        assertThat(result.get().getAuthor().getNickname()).isEqualTo("작성자");
    }

    @Test
    void findByIdWithAuthor_존재하지않음_빈Optional() {
        // when
        Optional<Comment> result = commentRepository.findByIdWithAuthor(UUID.randomUUID());

        // then
        assertThat(result).isEmpty();
    }
}
