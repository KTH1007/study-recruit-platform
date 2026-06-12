package com.study.platform.domain.apply.model;

import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ApplyRepositoryTest extends AbstractIntegrationTest {

    @Autowired private ApplyRepository applyRepository;
    @Autowired private StudyPostRepository studyPostRepository;
    @Autowired private UserRepository userRepository;

    private User author;
    private User applicant;
    private StudyPost post;
    private Apply apply;

    @BeforeEach
    void setUp() {
        author = userRepository.save(User.create("kakao-1", "작성자", "author@test.com"));
        applicant = userRepository.save(User.create("kakao-2", "지원자", "applicant@test.com"));
        post = studyPostRepository.save(StudyPost.create(
                author, "스터디 모집", "열심히 합니다", "Java", 5,
                LocalDateTime.now().plusDays(7)
        ));
        apply = applyRepository.save(Apply.create(post, applicant, "지원합니다"));
    }

    @Test
    void findAllByPostIdWithApplicant_성공() {
        List<Apply> result = applyRepository.findAllByPostIdWithApplicant(post.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApplicant().getNickname()).isEqualTo("지원자");
        assertThat(result.get(0).getMessage()).isEqualTo("지원합니다");
    }

    @Test
    void findAllByPostIdWithApplicant_지원없음_빈리스트() {
        StudyPost otherPost = studyPostRepository.save(StudyPost.create(
                author, "다른 스터디", "열심히 합니다", "Kotlin", 3,
                LocalDateTime.now().plusDays(7)
        ));

        List<Apply> result = applyRepository.findAllByPostIdWithApplicant(otherPost.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdWithPostAndApplicant_성공() {
        Optional<Apply> result = applyRepository.findByIdWithPostAndApplicant(apply.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getPost().getTitle()).isEqualTo("스터디 모집");
        assertThat(result.get().getApplicant().getNickname()).isEqualTo("지원자");
    }

    @Test
    void findByIdWithPostAndApplicant_존재하지않음_빈Optional() {
        Optional<Apply> result = applyRepository.findByIdWithPostAndApplicant(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void findByPostIdAndApplicantId_성공() {
        Optional<Apply> result = applyRepository.findByPostIdAndApplicantId(post.getId(), applicant.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(ApplyStatus.PENDING);
    }

    @Test
    void findByPostIdAndApplicantId_존재하지않음_빈Optional() {
        Optional<Apply> result = applyRepository.findByPostIdAndApplicantId(post.getId(), author.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void existsByPostIdAndApplicantId_존재함_true() {
        boolean result = applyRepository.existsByPostIdAndApplicantId(post.getId(), applicant.getId());

        assertThat(result).isTrue();
    }

    @Test
    void existsByPostIdAndApplicantId_존재하지않음_false() {
        boolean result = applyRepository.existsByPostIdAndApplicantId(post.getId(), author.getId());

        assertThat(result).isFalse();
    }

    @Test
    void countByPostIdAndStatus_PENDING_카운트() {
        long count = applyRepository.countByPostIdAndStatus(post.getId(), ApplyStatus.PENDING);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void countByPostIdAndStatus_APPROVED_카운트() {
        apply.approve();
        applyRepository.save(apply);

        long count = applyRepository.countByPostIdAndStatus(post.getId(), ApplyStatus.APPROVED);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void countByPostIdAndStatus_없을때_0() {
        long count = applyRepository.countByPostIdAndStatus(post.getId(), ApplyStatus.APPROVED);

        assertThat(count).isZero();
    }

    @Test
    void findByIdWithPostAndApplicantForUpdate_성공() {
        Optional<Apply> result = applyRepository.findByIdWithPostAndApplicantForUpdate(apply.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(apply.getId());
    }
}
