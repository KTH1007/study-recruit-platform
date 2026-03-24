package com.study.platform.domain.notification.application;

public interface NotificationHandler<T> {

    void handle(T event);
}
