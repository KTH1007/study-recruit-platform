package com.study.platform.support.fake;

import com.study.platform.global.auth.port.TokenRepository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeTokenRepository implements TokenRepository {

    private final Map<String, String> store = new HashMap<>();

    @Override
    public void save(String key, String value, Duration ttl) {
        store.put(key, value);
    }

    @Override
    public Optional<String> find(String key) {
        return Optional.ofNullable(store.get(key));
    }

    @Override
    public void delete(String key) {
        store.remove(key);
    }
}
