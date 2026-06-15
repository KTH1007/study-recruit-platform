package com.study.platform.support.fake;

import com.study.platform.global.outbox.model.OutboxEvent;
import com.study.platform.global.outbox.model.OutboxEventRepository;
import com.study.platform.global.outbox.model.OutboxEventStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class FakeOutboxEventRepository implements OutboxEventRepository {

    private final List<OutboxEvent> store = new ArrayList<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public OutboxEvent save(OutboxEvent event) {
        if (event.getId() == null) {
            ReflectionTestUtils.setField(event, "id", sequence.getAndIncrement());
        }
        store.add(event);
        return event;
    }

    @Override
    public List<OutboxEvent> findAllByStatusAndCreatedAtBefore(OutboxEventStatus status, LocalDateTime createdAt) {
        return store.stream()
                .filter(e -> e.getStatus() == status)
                .toList();
    }

    @Override
    public int markSentById(Long id) {
        return store.stream()
                .filter(e -> e.getId().equals(id))
                .mapToInt(e -> {
                    ReflectionTestUtils.setField(e, "status", OutboxEventStatus.SENT);
                    return 1;
                })
                .sum();
    }

    @Override
    public void markFailedPermanently(Long id) {
        store.stream()
                .filter(e -> e.getId().equals(id))
                .forEach(e -> ReflectionTestUtils.setField(e, "status", OutboxEventStatus.FAILED_PERMANENTLY));
    }
}
