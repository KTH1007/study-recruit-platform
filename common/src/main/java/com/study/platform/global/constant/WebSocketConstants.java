package com.study.platform.global.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class WebSocketConstants {

    public static final String BROKER_PREFIX = "/topic";
    public static final String APP_PREFIX = "/app";
    public static final String ENDPOINT = "/ws";
    public static final String CHAT_TOPIC_PREFIX = "/topic/chat/";
    public static final String CHAT_DESTINATION_PREFIX = "/app/chat/";
    public static final String REDIS_CHAT_CHANNEL_PREFIX = "chat:";
}
