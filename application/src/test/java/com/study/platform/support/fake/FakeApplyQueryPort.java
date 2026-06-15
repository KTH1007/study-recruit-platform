package com.study.platform.support.fake;

import com.study.platform.domain.apply.dto.response.ApplyResponse;
import com.study.platform.domain.apply.port.ApplyQueryPort;

import java.util.List;
import java.util.UUID;

public class FakeApplyQueryPort implements ApplyQueryPort {

    private final FakeApplyRepository repository;

    public FakeApplyQueryPort(FakeApplyRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ApplyResponse> findAllByPostId(UUID postId) {
        return repository.findAllByPostId(postId).stream()
                .map(ApplyResponse::from)
                .toList();
    }
}
