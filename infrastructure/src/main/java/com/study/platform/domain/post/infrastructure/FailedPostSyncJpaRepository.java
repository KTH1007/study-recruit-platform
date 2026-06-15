package com.study.platform.domain.post.infrastructure;

import com.study.platform.domain.post.model.FailedPostSync;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FailedPostSyncJpaRepository extends JpaRepository<FailedPostSync, UUID> {
}
