package com.study.platform.domain.post.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PostSearchPort {

    Page<PostSearchResult> search(String keyword, String techStack, StudyPostStatus status,
                                  int maxMembers, Pageable pageable);

    void index(StudyPost post);

    void indexAll(List<StudyPost> posts);

    void delete(UUID postId);
}
