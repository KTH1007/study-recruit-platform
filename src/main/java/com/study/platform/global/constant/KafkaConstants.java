package com.study.platform.global.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaConstants {

    public static final String NOTIFICATION_TOPIC = "notification";
    public static final String NOTIFICATION_GROUP = "notification-group";

    public static final String POST_SYNC_TOPIC = "post-sync";
    public static final String POST_SYNC_GROUP = "post-sync-group";

    public static final String POST_SYNC_DLT_TOPIC = "post-sync.DLT";
    public static final String POST_SYNC_DLT_GROUP = "post-sync-dlt-group";
}