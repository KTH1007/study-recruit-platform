package com.study.platform.domain.notification.application

interface NotificationHandler<T> {
    fun handle(event: T)
}
