package com.study.platform.domain.user.infrastructure;

import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id);
    }

    @Override
    public Optional<User> findByKakaoId(String kakaoId) {
        return userJpaRepository.findByKakaoId(kakaoId);
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        return userJpaRepository.findByNickname(nickname);
    }

    @Override
    public List<User> findAllByNicknameIn(Collection<String> nicknames) {
        return userJpaRepository.findAllByNicknameIn(nicknames);
    }
}
