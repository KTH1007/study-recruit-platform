package com.study.platform.global.auth.port;

import java.time.Duration;
import java.util.Optional;

public interface TokenRepository {
    void save(String key, String value, Duration ttl);
    Optional<String> find(String key);
    void delete(String key);
}