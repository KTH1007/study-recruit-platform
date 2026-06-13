package com.study.platform.domain.apply.application;

import com.study.platform.domain.apply.dto.request.ApplyCreateRequest;
import com.study.platform.domain.apply.dto.response.ApplyResponse;
import com.study.platform.domain.apply.event.ApplyApprovedEvent;
import com.study.platform.domain.apply.event.ApplyReceivedEvent;
import com.study.platform.domain.apply.event.ApplyRejectedEvent;
import com.study.platform.domain.apply.model.Apply;
import com.study.platform.domain.apply.model.ApplyStatus;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.user.model.User;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.support.fake.FakeApplyRepository;
import com.study.platform.support.fake.FakeDomainEventPublisher;
import com.study.platform.support.fake.FakeStudyPostRepository;
import com.study.platform.support.fake.FakeUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplyServiceTest {

    private FakeApplyRepository applyRepository;
    private FakeStudyPostRepository studyPostRepository;
    private FakeUserRepository userRepository;
    private FakeDomainEventPublisher eventPublisher;
    private ApplyService applyService;

    private UUID authorId;
    private UUID applicantId;
    private UUID postId;
    private User author;
    private User applicant;
    private StudyPost post;

    @BeforeEach
    void setUp() {
        applyRepository = new FakeApplyRepository();
        studyPostRepository = new FakeStudyPostRepository();
        userRepository = new FakeUserRepository();
        eventPublisher = new FakeDomainEventPublisher();
        applyService = new ApplyService(applyRepository, studyPostRepository, userRepository, eventPublisher);

        authorId = UUID.randomUUID();
        applicantId = UUID.randomUUID();
        postId = UUID.randomUUID();

        author = User.create("kakao1", "작성자", "author@test.com");
        ReflectionTestUtils.setField(author, "id", authorId);

        applicant = User.create("kakao2", "지원자", "applicant@test.com");
        ReflectionTestUtils.setField(applicant, "id", applicantId);

        post = StudyPost.create(author, "스터디 모집", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(post, "id", postId);

        studyPostRepository.save(post);
        userRepository.save(applicant);
    }

    @Test
    void apply_성공() {
        // given
        ApplyCreateRequest request = new ApplyCreateRequest("지원합니다");

        // when
        ApplyResponse response = applyService.apply(applicantId, postId, request);

        // then
        assertThat(response.status()).isEqualTo(ApplyStatus.PENDING);
        assertThat(applyRepository.existsByPostIdAndApplicantId(postId, applicantId)).isTrue();
        assertThat(eventPublisher.hasEventOf(ApplyReceivedEvent.class)).isTrue();
    }

    @Test
    void apply_게시글없음_예외발생() {
        // given
        ApplyCreateRequest request = new ApplyCreateRequest("지원합니다");

        // when & then
        assertThatThrownBy(() -> applyService.apply(applicantId, UUID.randomUUID(), request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
    }

    @Test
    void apply_마감된게시글_예외발생() {
        // given
        post.close();
        ApplyCreateRequest request = new ApplyCreateRequest("지원합니다");

        // when & then
        assertThatThrownBy(() -> applyService.apply(applicantId, postId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_CLOSED);
    }

    @Test
    void apply_본인게시글지원_예외발생() {
        // given
        ApplyCreateRequest request = new ApplyCreateRequest("지원합니다");

        // when & then
        assertThatThrownBy(() -> applyService.apply(authorId, postId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_APPLY_OWN_POST);
    }

    @Test
    void apply_중복지원_예외발생() {
        // given
        Apply existing = Apply.create(post, applicant, "첫 번째 지원", eventPublisher);
        ReflectionTestUtils.setField(existing, "id", UUID.randomUUID());
        applyRepository.save(existing);
        ApplyCreateRequest request = new ApplyCreateRequest("두 번째 지원");

        // when & then
        assertThatThrownBy(() -> applyService.apply(applicantId, postId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_APPLIED);
    }

    @Test
    void approve_성공() {
        // given
        Apply apply = Apply.create(post, applicant, "지원합니다", eventPublisher);
        UUID applyId = UUID.randomUUID();
        ReflectionTestUtils.setField(apply, "id", applyId);
        applyRepository.save(apply);

        // when
        ApplyResponse response = applyService.approve(authorId, applyId);

        // then
        assertThat(response.status()).isEqualTo(ApplyStatus.APPROVED);
        assertThat(eventPublisher.hasEventOf(ApplyApprovedEvent.class)).isTrue();
    }

    @Test
    void approve_정원충족시_게시글마감() {
        // given - 이미 승인된 지원 2개 세팅 (maxMembers=3)
        for (int i = 0; i < 2; i++) {
            User u = User.create("kakao" + i, "user" + i, "u" + i + "@test.com");
            ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
            Apply approved = Apply.create(post, u, "지원", eventPublisher);
            ReflectionTestUtils.setField(approved, "id", UUID.randomUUID());
            applyRepository.save(approved);
            approved.approve(eventPublisher);
        }
        Apply apply = Apply.create(post, applicant, "지원합니다", eventPublisher);
        UUID applyId = UUID.randomUUID();
        ReflectionTestUtils.setField(apply, "id", applyId);
        applyRepository.save(apply);

        // when
        applyService.approve(authorId, applyId);

        // then
        assertThat(post.isOpen()).isFalse();
    }

    @Test
    void reject_성공() {
        // given
        Apply apply = Apply.create(post, applicant, "지원합니다", eventPublisher);
        UUID applyId = UUID.randomUUID();
        ReflectionTestUtils.setField(apply, "id", applyId);
        applyRepository.save(apply);

        // when
        ApplyResponse response = applyService.reject(authorId, applyId);

        // then
        assertThat(response.status()).isEqualTo(ApplyStatus.REJECTED);
        assertThat(eventPublisher.hasEventOf(ApplyRejectedEvent.class)).isTrue();
    }

    @Test
    void cancel_성공() {
        // given
        Apply apply = Apply.create(post, applicant, "지원합니다", eventPublisher);
        UUID applyId = UUID.randomUUID();
        ReflectionTestUtils.setField(apply, "id", applyId);
        applyRepository.save(apply);

        // when
        applyService.cancel(applicantId, postId);

        // then
        assertThat(applyRepository.findById(applyId)).isEmpty();
    }

    @Test
    void findApplies_성공() {
        // given
        userRepository.save(author);
        Apply apply = Apply.create(post, applicant, "지원합니다", eventPublisher);
        ReflectionTestUtils.setField(apply, "id", UUID.randomUUID());
        applyRepository.save(apply);

        // when
        List<ApplyResponse> responses = applyService.findApplies(authorId, postId);

        // then
        assertThat(responses).hasSize(1);
    }

    @Test
    void findApplies_작성자아님_예외발생() {
        // given
        UUID otherId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> applyService.findApplies(otherId, postId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }
}
