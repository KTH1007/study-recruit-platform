package com.study.platform.global.redis;

public interface RedisMessagePublisher {

    void publish(String channel, String message);
}
