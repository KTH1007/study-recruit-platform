package com.study.platform.domain.post.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FailedPostSyncRepository extends JpaRepository<FailedPostSync, UUID> {
}