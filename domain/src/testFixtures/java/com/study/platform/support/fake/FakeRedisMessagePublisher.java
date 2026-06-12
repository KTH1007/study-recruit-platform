package com.study.platform.support.fake;

import com.study.platform.global.redis.RedisMessagePublisher;

import java.util.ArrayList;
import java.util.List;

public class FakeRedisMessagePublisher implements RedisMessagePublisher {

    private final List<String> publishedChannels = new ArrayList<>();

    @Override
    public void publish(String channel, String message) {
        publishedChannels.add(channel);
    }

    public boolean wasPublishedTo(String channel) {
        return publishedChannels.contains(channel);
    }

    public List<String> getPublishedChannels() {
        return publishedChannels;
    }
}
