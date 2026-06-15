package com.study.platform.domain.apply.usecase;

import com.study.platform.domain.apply.dto.response.ApplyResponse;

import java.util.List;
import java.util.UUID;

public interface FindAppliesUseCase {
    List<ApplyResponse> execute(UUID userId, UUID postId);
}
