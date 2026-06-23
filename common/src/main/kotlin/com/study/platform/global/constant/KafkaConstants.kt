package com.study.platform.global.constant

object KafkaConstants {
    const val NOTIFICATION_TOPIC = "notification"
    const val NOTIFICATION_GROUP = "notification-group"

    const val POST_SYNC_TOPIC = "post-sync"
    const val POST_SYNC_GROUP = "post-sync-group"

    const val POST_SYNC_DLT_TOPIC = "post-sync.DLT"
    const val POST_SYNC_DLT_GROUP = "post-sync-dlt-group"

    const val NOTIFICATION_DLT_TOPIC = "notification.DLT"
    const val NOTIFICATION_DLT_GROUP = "notification-dlt-group"
    const val MAX_DLT_RETRY = 3
}
