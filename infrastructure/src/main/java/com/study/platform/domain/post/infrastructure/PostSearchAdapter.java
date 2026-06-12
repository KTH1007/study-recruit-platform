package com.study.platform.domain.post.infrastructure;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.study.platform.domain.post.document.PostDocument;
import com.study.platform.domain.post.document.PostSearchRepository;
import com.study.platform.domain.post.model.PostSearchPort;
import com.study.platform.domain.post.model.PostSearchResult;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostSearchAdapter implements PostSearchPort {

    private final ElasticsearchOperations elasticsearchOperations;
    private final PostSearchRepository postSearchRepository;

    @Override
    public Page<PostSearchResult> search(String keyword, String techStack, StudyPostStatus status,
                                          int maxMembers, Pageable pageable) {
        NativeQuery query = buildQuery(keyword, techStack, status, maxMembers, pageable);
        SearchHits<PostDocument> hits = elasticsearchOperations.search(query, PostDocument.class);
        List<PostSearchResult> results = hits.stream()
                .map(SearchHit::getContent)
                .map(this::toResult)
                .toList();
        return new PageImpl<>(results, pageable, hits.getTotalHits());
    }

    @Override
    public void index(StudyPost post) {
        postSearchRepository.save(PostDocument.from(post));
    }

    @Override
    public void indexAll(List<StudyPost> posts) {
        postSearchRepository.saveAll(posts.stream().map(PostDocument::from).toList());
    }

    @Override
    public void delete(UUID postId) {
        postSearchRepository.deleteById(postId.toString());
    }

    private PostSearchResult toResult(PostDocument doc) {
        return new PostSearchResult(
                UUID.fromString(doc.getId()),
                doc.getAuthorNickname(),
                doc.getTitle(),
                doc.getTechStack(),
                doc.getMaxMembers(),
                doc.getDeadline(),
                StudyPostStatus.valueOf(doc.getStatus()),
                doc.getCreatedAt()
        );
    }

    private NativeQuery buildQuery(String keyword, String techStack, StudyPostStatus status,
                                   int maxMembers, Pageable pageable) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        if (keyword != null && !keyword.isBlank()) {
            boolQuery.must(MultiMatchQuery.of(m -> m
                    .fields("title", "description")
                    .query(keyword)
                    .type(TextQueryType.BestFields)
            )._toQuery());
        }

        if (techStack != null && !techStack.isBlank()) {
            boolQuery.filter(MatchQuery.of(m -> m
                    .field("techStack")
                    .query(techStack)
            )._toQuery());
        }

        if (status != null) {
            boolQuery.filter(TermQuery.of(t -> t
                    .field("status")
                    .value(status.name())
            )._toQuery());
        }

        if (maxMembers > 0) {
            boolQuery.filter(RangeQuery.of(r -> r
                    .number(n -> n.field("maxMembers").lte((double) maxMembers))
            )._toQuery());
        }

        return NativeQuery.builder()
                .withQuery(boolQuery.build()._toQuery())
                .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)))
                .withPageable(pageable)
                .build();
    }
}
