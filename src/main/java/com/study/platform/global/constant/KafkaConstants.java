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

    public static final String NOTIFICATION_DLT_TOPIC = "notification.DLT";
    public static final String NOTIFICATION_DLT_GROUP = "notification-dlt-group";
}