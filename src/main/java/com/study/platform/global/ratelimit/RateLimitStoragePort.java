package com.study.platform.global.ratelimit;

public interface RateLimitStoragePort {

    boolean isAllowed(String key, long windowSeconds, long limit);
}
