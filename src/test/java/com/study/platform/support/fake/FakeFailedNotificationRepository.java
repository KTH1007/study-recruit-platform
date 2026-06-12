package com.study.platform.support.fake;

import com.study.platform.domain.notification.model.FailedNotification;
import com.study.platform.domain.notification.model.FailedNotificationRepository;

import java.util.ArrayList;
import java.util.List;

public class FakeFailedNotificationRepository implements FailedNotificationRepository {

    private final List<FailedNotification> store = new ArrayList<>();

    @Override
    public FailedNotification save(FailedNotification failedNotification) {
        store.add(failedNotification);
        return failedNotification;
    }

    public List<FailedNotification> getSaved() {
        return store;
    }
}
