package com.study.platform.domain.post.application;

import com.study.platform.domain.post.model.PostSearchPort;
import com.study.platform.domain.post.model.PostSearchResult;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostSearchService {

    private final PostSearchPort postSearchPort;

    @CircuitBreaker(name = "elasticsearch", fallbackMethod = "searchFallback")
    public Page<PostSearchResult> search(String keyword, String techStack, StudyPostStatus status,
                                         int maxMembers, Pageable pageable) {
        return postSearchPort.search(keyword, techStack, status, maxMembers, pageable);
    }

    @CircuitBreaker(name = "elasticsearch")
    public void index(StudyPost post) {
        postSearchPort.index(post);
    }

    @CircuitBreaker(name = "elasticsearch")
    public void indexAll(List<StudyPost> posts) {
        postSearchPort.indexAll(posts);
    }

    @CircuitBreaker(name = "elasticsearch")
    public void delete(String id) {
        postSearchPort.delete(java.util.UUID.fromString(id));
    }

    private Page<PostSearchResult> searchFallback(String keyword, String techStack, StudyPostStatus status,
                                                   int maxMembers, Pageable pageable, Exception e) {
        log.warn("Elasticsearch 장애로 검색 불가. keyword={}", keyword, e);
        return Page.empty(pageable);
    }
}
