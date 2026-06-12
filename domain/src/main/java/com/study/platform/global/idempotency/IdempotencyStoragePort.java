package com.study.platform.global.idempotency;

import java.time.Duration;

public interface IdempotencyStoragePort {

    String get(String key);

    boolean setIfAbsent(String key, String value, Duration ttl);

    void set(String key, String value, Duration ttl);

    void delete(String key);
}
