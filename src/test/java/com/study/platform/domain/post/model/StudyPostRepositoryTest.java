package com.study.platform.domain.post.model;

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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class StudyPostRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private StudyPostRepository studyPostRepository;

    @Autowired
    private UserRepository userRepository;

    private User author;
    private StudyPost post;

    @BeforeEach
    void setUp() {
        author = userRepository.save(User.create("kakao-1", "작성자", "author@test.com"));
        post = studyPostRepository.save(StudyPost.create(
                author, "스터디 모집", "열심히 합니다", "Java", 5,
                LocalDateTime.now().plusDays(7)
        ));
    }

    @Test
    void findByIdWithAuthor_성공() {
        // when
        Optional<StudyPost> result = studyPostRepository.findByIdWithAuthor(post.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("스터디 모집");
        assertThat(result.get().getAuthor().getNickname()).isEqualTo("작성자");
    }

    @Test
    void findByIdWithAuthor_존재하지않음_빈Optional() {
        // when
        Optional<StudyPost> result = studyPostRepository.findByIdWithAuthor(java.util.UUID.randomUUID());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByIdWithAuthorForUpdate_성공() {
        // when
        Optional<StudyPost> result = studyPostRepository.findByIdWithAuthorForUpdate(post.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(post.getId());
    }

    @Test
    void findAllWithFilter_techStack_필터링() {
        // given
        studyPostRepository.save(StudyPost.create(
                author, "Kotlin 스터디", "열심히 합니다", "Kotlin", 3,
                LocalDateTime.now().plusDays(7)
        ));

        // when
        Page<StudyPost> result = studyPostRepository.findAllWithFilter(
                "Java", null, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTechStack()).isEqualTo("Java");
    }

    @Test
    void findAllWithFilter_status_필터링() {
        // given
        StudyPost closedPost = studyPostRepository.save(StudyPost.create(
                author, "마감된 스터디", "열심히 합니다", "Python", 3,
                LocalDateTime.now().plusDays(7)
        ));
        closedPost.close();
        studyPostRepository.save(closedPost);

        // when
        Page<StudyPost> result = studyPostRepository.findAllWithFilter(
                null, StudyPostStatus.OPEN, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(StudyPostStatus.OPEN);
    }

    @Test
    void findAllWithFilter_필터없음_전체조회() {
        // given
        studyPostRepository.save(StudyPost.create(
                author, "두 번째 스터디", "열심히 합니다", "Kotlin", 3,
                LocalDateTime.now().plusDays(7)
        ));

        // when
        Page<StudyPost> result = studyPostRepository.findAllWithFilter(
                null, null, PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findExpiredPosts_마감기한지난게시글_조회() {
        // given
        studyPostRepository.save(StudyPost.create(
                author, "기한 지난 스터디", "열심히 합니다", "Java", 3,
                LocalDateTime.now().minusDays(1)
        ));

        // when
        List<StudyPost> result = studyPostRepository.findExpiredPosts(
                LocalDateTime.now(), StudyPostStatus.OPEN);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("기한 지난 스터디");
    }

    @Test
    void findDeadlineReminderPosts_마감임박게시글_조회() {
        // given
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        studyPostRepository.save(StudyPost.create(
                author, "마감 임박 스터디", "열심히 합니다", "Java", 3, tomorrow
        ));

        // when
        List<StudyPost> result = studyPostRepository.findDeadlineReminderPosts(
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(2),
                StudyPostStatus.OPEN
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("마감 임박 스터디");
    }
}
