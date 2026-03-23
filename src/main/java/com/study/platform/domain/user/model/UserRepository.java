package com.study.platform.domain.user.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByKakaoId(String kakaoId);

    Optional<User> findByNickname(String nickname);
}
