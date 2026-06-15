package com.study.platform.domain.user.model;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByKakaoId(String kakaoId);
    Optional<User> findByNickname(String nickname);
    List<User> findAllByNicknameIn(Collection<String> nicknames);
}
