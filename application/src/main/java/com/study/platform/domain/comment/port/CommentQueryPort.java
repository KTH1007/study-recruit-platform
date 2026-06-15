package com.study.platform.domain.comment.port;

import com.study.platform.domain.comment.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CommentQueryPort {

    Page<CommentResponse> findAllByPostId(UUID postId, Pageable pageable);
}
