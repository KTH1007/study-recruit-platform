package com.study.platform.global.event;

public interface DomainEventPublisher {
    void publish(Object object);
}
