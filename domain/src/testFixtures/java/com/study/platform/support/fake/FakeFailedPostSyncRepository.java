package com.study.platform.support.fake;

import com.study.platform.domain.post.model.FailedPostSync;
import com.study.platform.domain.post.model.FailedPostSyncRepository;

import java.util.ArrayList;
import java.util.List;

public class FakeFailedPostSyncRepository implements FailedPostSyncRepository {

    private final List<FailedPostSync> store = new ArrayList<>();

    @Override
    public FailedPostSync save(FailedPostSync failedPostSync) {
        store.add(failedPostSync);
        return failedPostSync;
    }

    public List<FailedPostSync> getSaved() {
        return store;
    }
}
