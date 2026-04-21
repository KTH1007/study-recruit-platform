package com.study.platform.domain.team.model;

import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StudyTeamRepositoryTest {

    @Autowired
    private StudyTeamRepository studyTeamRepository;

    @Autowired
    private StudyPostRepository studyPostRepository;

    @Autowired
    private UserRepository userRepository;

    private StudyPost post;
    private StudyTeam team;

    @BeforeEach
    void setUp() {
        User author = userRepository.save(User.create("kakao-1", "작성자", "author@test.com"));
        post = studyPostRepository.save(StudyPost.create(
                author, "스터디 모집", "열심히 합니다", "Java", 5,
                LocalDateTime.now().plusDays(7)
        ));
        team = studyTeamRepository.save(StudyTeam.create(post));
    }

    @Test
    void findByPostId_성공() {
        // when
        Optional<StudyTeam> result = studyTeamRepository.findByPostId(post.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("스터디 모집");
    }

    @Test
    void findByPostId_존재하지않음_빈Optional() {
        // when
        Optional<StudyTeam> result = studyTeamRepository.findByPostId(UUID.randomUUID());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void save_팀생성_성공() {
        // when & then
        assertThat(team.getId()).isNotNull();
        assertThat(team.getName()).isEqualTo(post.getTitle());
        assertThat(team.getPost().getId()).isEqualTo(post.getId());
    }
}
