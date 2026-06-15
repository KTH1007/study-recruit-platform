package com.study.platform.domain.apply.usecase;

import com.study.platform.domain.apply.dto.response.ApplyResponse;

import java.util.UUID;

public interface RejectApplyUseCase {
    ApplyResponse execute(UUID userId, UUID applyId);
}
