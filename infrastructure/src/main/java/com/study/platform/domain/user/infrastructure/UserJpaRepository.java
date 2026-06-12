package com.study.platform.domain.user.infrastructure;

import com.study.platform.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<User, UUID> {

    Optional<User> findByKakaoId(String kakaoId);

    Optional<User> findByNickname(String nickname);

    List<User> findAllByNicknameIn(Collection<String> nicknames);
}
