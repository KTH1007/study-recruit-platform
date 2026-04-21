package com.study.platform.domain.comment.application;

import com.study.platform.domain.comment.dto.request.CommentCreateRequest;
import com.study.platform.domain.comment.dto.request.CommentUpdateRequest;
import com.study.platform.domain.comment.dto.response.CommentResponse;
import com.study.platform.domain.comment.event.CommentCreatedEvent;
import com.study.platform.domain.comment.event.MentionEvent;
import com.study.platform.domain.comment.model.Comment;
import com.study.platform.domain.comment.model.CommentRepository;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private StudyPostRepository studyPostRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CommentService commentService;

    private UUID authorId;
    private UUID commenterId;
    private UUID postId;
    private UUID commentId;
    private User author;
    private User commenter;
    private StudyPost post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();
        commenterId = UUID.randomUUID();
        postId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        author = User.create("kakao1", "작성자", "author@test.com");
        ReflectionTestUtils.setField(author, "id", authorId);

        commenter = User.create("kakao2", "댓글작성자", "commenter@test.com");
        ReflectionTestUtils.setField(commenter, "id", commenterId);

        post = StudyPost.create(author, "스터디 모집", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(post, "id", postId);

        comment = Comment.create(post, commenter, "좋은 스터디네요");
        ReflectionTestUtils.setField(comment, "id", commentId);
    }

    @Test
    void createComment_성공() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("좋은 스터디네요");
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.of(post));
        given(userRepository.findById(commenterId)).willReturn(Optional.of(commenter));
        given(userRepository.findAllByNicknameIn(any())).willReturn(List.of());

        // when
        CommentResponse response = commentService.createComment(commenterId, postId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.content()).isEqualTo("좋은 스터디네요");
        then(commentRepository).should().save(any(Comment.class));
    }

    @Test
    void createComment_게시글없음_예외발생() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("좋은 스터디네요");
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(commenterId, postId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
    }

    @Test
    void createComment_사용자없음_예외발생() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("좋은 스터디네요");
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.of(post));
        given(userRepository.findById(commenterId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(commenterId, postId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void createComment_작성자댓글시_방장알림미발행() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("제 글에 댓글 달아요");
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.of(post));
        given(userRepository.findById(authorId)).willReturn(Optional.of(author));
        given(userRepository.findAllByNicknameIn(any())).willReturn(List.of());

        // when
        commentService.createComment(authorId, postId, request);

        // then
        then(eventPublisher).should(never()).publishEvent(any(CommentCreatedEvent.class));
    }

    @Test
    void createComment_멘션포함시_멘션알림발행() {
        // given
        User mentionedUser = User.create("kakao3", "멘션대상", "mentioned@test.com");
        ReflectionTestUtils.setField(mentionedUser, "id", UUID.randomUUID());

        CommentCreateRequest request = new CommentCreateRequest("@멘션대상 확인해주세요");
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.of(post));
        given(userRepository.findById(commenterId)).willReturn(Optional.of(commenter));
        given(userRepository.findAllByNicknameIn(any())).willReturn(List.of(mentionedUser));

        // when
        commentService.createComment(commenterId, postId, request);

        // then
        then(eventPublisher).should().publishEvent(any(MentionEvent.class));
    }

    @Test
    void createComment_본인멘션시_멘션알림미발행() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("@댓글작성자 본인 멘션");
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.of(post));
        given(userRepository.findById(commenterId)).willReturn(Optional.of(commenter));
        given(userRepository.findAllByNicknameIn(any())).willReturn(List.of(commenter));

        // when
        commentService.createComment(commenterId, postId, request);

        // then
        then(eventPublisher).should(never()).publishEvent(any(MentionEvent.class));
    }

    @Test
    void updateComment_성공() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");
        given(commentRepository.findByIdWithAuthor(commentId)).willReturn(Optional.of(comment));

        // when
        CommentResponse response = commentService.updateComment(commenterId, commentId, request);

        // then
        assertThat(response.content()).isEqualTo("수정된 댓글");
    }

    @Test
    void updateComment_작성자아님_예외발생() {
        // given
        UUID otherId = UUID.randomUUID();
        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");
        given(commentRepository.findByIdWithAuthor(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(otherId, commentId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_COMMENT_AUTHOR);
    }

    @Test
    void updateComment_댓글없음_예외발생() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");
        given(commentRepository.findByIdWithAuthor(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(commenterId, commentId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
    }

    @Test
    void deleteComment_성공() {
        // given
        given(commentRepository.findByIdWithAuthor(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.deleteComment(commenterId, commentId);

        // then
        then(commentRepository).should().delete(comment);
    }

    @Test
    void deleteComment_작성자아님_예외발생() {
        // given
        UUID otherId = UUID.randomUUID();
        given(commentRepository.findByIdWithAuthor(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(otherId, commentId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_COMMENT_AUTHOR);
    }

    @Test
    void deleteComment_댓글없음_예외발생() {
        // given
        given(commentRepository.findByIdWithAuthor(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(commenterId, commentId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
    }
}
