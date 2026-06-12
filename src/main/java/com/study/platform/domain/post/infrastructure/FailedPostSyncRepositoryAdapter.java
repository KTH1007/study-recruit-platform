package com.study.platform.domain.post.infrastructure;

import com.study.platform.domain.post.model.FailedPostSync;
import com.study.platform.domain.post.model.FailedPostSyncRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FailedPostSyncRepositoryAdapter implements FailedPostSyncRepository {

    private final FailedPostSyncJpaRepository failedPostSyncJpaRepository;

    @Override
    public FailedPostSync save(FailedPostSync failedPostSync) {
        return failedPostSyncJpaRepository.save(failedPostSync);
    }
}
