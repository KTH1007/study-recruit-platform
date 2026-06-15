package com.study.platform.global.idempotency;

import java.time.Duration;

public interface IdempotencyObjectStoragePort {

    Object get(String key);

    boolean setIfAbsent(String key, String marker, Duration ttl);

    void set(String key, Object value, Duration ttl);

    void delete(String key);
}
