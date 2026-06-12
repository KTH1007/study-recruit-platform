package com.study.platform.support.fake;

import com.study.platform.global.event.DomainEventPublisher;

import java.util.ArrayList;
import java.util.List;

public class FakeDomainEventPublisher implements DomainEventPublisher {

    private final List<Object> events = new ArrayList<>();

    @Override
    public void publish(Object event) {
        events.add(event);
    }

    public List<Object> getEvents() {
        return events;
    }

    // 특정 타입 이벤트만 꺼낼 때
    public <T> List<T> getEventsOf(Class<T> type) {
        return events.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }

    public boolean hasEventOf(Class<?> type) {
        return events.stream().anyMatch(type::isInstance);
    }
}
