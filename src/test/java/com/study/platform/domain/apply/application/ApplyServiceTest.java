package com.study.platform.domain.apply.application;

import com.study.platform.domain.apply.dto.request.ApplyCreateRequest;
import com.study.platform.domain.apply.dto.response.ApplyResponse;
import com.study.platform.domain.apply.event.ApplyApprovedEvent;
import com.study.platform.domain.apply.event.ApplyReceivedEvent;
import com.study.platform.domain.apply.event.ApplyRejectedEvent;
import com.study.platform.domain.apply.model.Apply;
import com.study.platform.domain.apply.model.ApplyRepository;
import com.study.platform.domain.apply.model.ApplyStatus;
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
import org.springframework.dao.DataIntegrityViolationException;
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

@ExtendWith(MockitoExtension.class)
class ApplyServiceTest {

    @Mock
    private ApplyRepository applyRepository;
    @Mock
    private StudyPostRepository studyPostRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ApplyService applyService;

    private UUID authorId;
    private UUID applicantId;
    private UUID postId;
    private UUID applyId;
    private User author;
    private User applicant;
    private StudyPost post;
    private Apply apply;

    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();
        applicantId = UUID.randomUUID();
        postId = UUID.randomUUID();
        applyId = UUID.randomUUID();

        author = User.create("kakao1", "작성자", "author@test.com");
        ReflectionTestUtils.setField(author, "id", authorId);

        applicant = User.create("kakao2", "지원자", "applicant@test.com");
        ReflectionTestUtils.setField(applicant, "id", applicantId);

        post = StudyPost.create(author, "스터디 모집", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(post, "id", postId);

        apply = Apply.create(post, applicant, "지원합니다");
        ReflectionTestUtils.setField(apply, "id", applyId);
    }

    @Test
    void apply_성공() {
        // given
        ApplyCreateRequest request = new ApplyCreateRequest("지원합니다");
        given(studyPostRepository.findByIdWithAuthorForUpdate(postId)).willReturn(Optional.of(post));
        given(userRepository.findById(applicantId)).willReturn(Optional.of(applicant));
        given(applyRepository.save(any())).willReturn(apply);

        // when
        ApplyResponse response = applyService.apply(applicantId, postId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ApplyStatus.PENDING);
        then(eventPublisher).should().publishEvent(any(ApplyReceivedEvent.class));
    }

    @Test
    void apply_게시글없음_예외발생() {
        // given
        ApplyCreateRequest request = new ApplyCreateRequest("지원합니다");
        given(studyPostRepository.findByIdWithAuthorForUpdate(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> applyService.apply(applicantId, postId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
    }

    @Test
    void apply_마감된게시글_예외발생() {
        // given
        ApplyCreateRequest request = new ApplyCreateRequest("지원합니다");
        post.close();
        given(studyPostRepository.findByIdWithAuthorForUpdate(postId)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> applyService.apply(applicantId, postId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_CLOSED);
    }

    @Test
    void apply_본인게시글지원_예외발생() {
        // given
        ApplyCreateRequest request = new ApplyCreateRequest("지원합니다");
        given(studyPostRepository.findByIdWithAuthorForUpdate(postId)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> applyService.apply(authorId, postId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_APPLY_OWN_POST);
    }

    @Test
    void apply_중복지원_예외발생() {
        // given
        ApplyCreateRequest request = new ApplyCreateRequest("지원합니다");
        given(studyPostRepository.findByIdWithAuthorForUpdate(postId)).willReturn(Optional.of(post));
        given(userRepository.findById(applicantId)).willReturn(Optional.of(applicant));
        given(applyRepository.save(any())).willThrow(DataIntegrityViolationException.class);

        // when & then
        assertThatThrownBy(() -> applyService.apply(applicantId, postId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_APPLIED);
    }

    @Test
    void approve_성공() {
        // given
        given(applyRepository.findByIdWithPostAndApplicant(applyId)).willReturn(Optional.of(apply));
        given(studyPostRepository.findByIdWithAuthorForUpdate(postId)).willReturn(Optional.of(post));
        given(applyRepository.findByIdWithPostAndApplicantForUpdate(applyId)).willReturn(Optional.of(apply));
        given(applyRepository.countByPostIdAndStatus(postId, ApplyStatus.APPROVED)).willReturn(1L);

        // when
        ApplyResponse response = applyService.approve(authorId, applyId);

        // then
        assertThat(response.status()).isEqualTo(ApplyStatus.APPROVED);
        then(eventPublisher).should().publishEvent(any(ApplyApprovedEvent.class));
    }

    @Test
    void approve_지원없음_예외발생() {
        // given
        given(applyRepository.findByIdWithPostAndApplicant(applyId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> applyService.approve(authorId, applyId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.APPLICATION_NOT_FOUND);
    }

    @Test
    void approve_작성자아님_예외발생() {
        // given
        UUID otherId = UUID.randomUUID();
        given(applyRepository.findByIdWithPostAndApplicant(applyId)).willReturn(Optional.of(apply));
        given(studyPostRepository.findByIdWithAuthorForUpdate(postId)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> applyService.approve(otherId, applyId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }

    @Test
    void approve_정원충족시_FULL상태변경() {
        // given
        given(applyRepository.findByIdWithPostAndApplicant(applyId)).willReturn(Optional.of(apply));
        given(studyPostRepository.findByIdWithAuthorForUpdate(postId)).willReturn(Optional.of(post));
        given(applyRepository.findByIdWithPostAndApplicantForUpdate(applyId)).willReturn(Optional.of(apply));
        given(applyRepository.countByPostIdAndStatus(postId, ApplyStatus.APPROVED)).willReturn(3L); // maxMembers = 3

        // when
        applyService.approve(authorId, applyId);

        // then
        assertThat(post.isOpen()).isFalse();
    }

    @Test
    void reject_성공() {
        // given
        given(applyRepository.findByIdWithPostAndApplicantForUpdate(applyId)).willReturn(Optional.of(apply));

        // when
        ApplyResponse response = applyService.reject(authorId, applyId);

        // then
        assertThat(response.status()).isEqualTo(ApplyStatus.REJECTED);
        then(eventPublisher).should().publishEvent(any(ApplyRejectedEvent.class));
    }

    @Test
    void reject_작성자아님_예외발생() {
        // given
        UUID otherId = UUID.randomUUID();
        given(applyRepository.findByIdWithPostAndApplicantForUpdate(applyId)).willReturn(Optional.of(apply));

        // when & then
        assertThatThrownBy(() -> applyService.reject(otherId, applyId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }

    @Test
    void cancel_성공() {
        // given
        given(applyRepository.findByPostIdAndApplicantId(postId, applicantId)).willReturn(Optional.of(apply));

        // when
        applyService.cancel(applicantId, postId);

        // then
        then(applyRepository).should().delete(apply);
    }

    @Test
    void cancel_지원없음_예외발생() {
        // given
        given(applyRepository.findByPostIdAndApplicantId(postId, applicantId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> applyService.cancel(applicantId, postId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.APPLICATION_NOT_FOUND);
    }

    @Test
    void findApplies_성공() {
        // given
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.of(post));
        given(applyRepository.findAllByPostIdWithApplicant(postId)).willReturn(List.of(apply));

        // when
        List<ApplyResponse> responses = applyService.findApplies(authorId, postId);

        // then
        assertThat(responses).hasSize(1);
    }

    @Test
    void findApplies_작성자아님_예외발생() {
        // given
        UUID otherId = UUID.randomUUID();
        given(studyPostRepository.findByIdWithAuthor(postId)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> applyService.findApplies(otherId, postId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }
}
