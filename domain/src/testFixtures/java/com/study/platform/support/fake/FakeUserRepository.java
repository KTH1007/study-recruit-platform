package com.study.platform.support.fake;

import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

public class FakeUserRepository implements UserRepository {

    private final Map<UUID, User> store = new HashMap<>();

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        }
        store.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<User> findByKakaoId(String kakaoId) {
        return store.values().stream()
                .filter(u -> u.getKakaoId().equals(kakaoId))
                .findFirst();
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        return store.values().stream()
                .filter(u -> u.getNickname().equals(nickname))
                .findFirst();
    }

    @Override
    public List<User> findAllByNicknameIn(Collection<String> nicknames) {
        return store.values().stream()
                .filter(u -> nicknames.contains(u.getNickname()))
                .toList();
    }
}
