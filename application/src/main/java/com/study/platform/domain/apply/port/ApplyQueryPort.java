package com.study.platform.domain.apply.port;

import com.study.platform.domain.apply.dto.response.ApplyResponse;

import java.util.List;
import java.util.UUID;

public interface ApplyQueryPort {

    List<ApplyResponse> findAllByPostId(UUID postId);
}
