package com.study.platform.domain.apply.usecase;

import java.util.UUID;

public interface CancelApplyUseCase {
    void execute(UUID userId, UUID postId);
}
