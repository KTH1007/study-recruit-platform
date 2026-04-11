package com.study.platform.domain.post.application;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.study.platform.domain.post.document.PostDocument;
import com.study.platform.domain.post.document.PostSearchRepository;
import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse;
import com.study.platform.domain.post.model.StudyPostStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final PostSearchRepository postSearchRepository;

    public Page<StudyPostSummaryResponse> search(String keyword, String techStack, StudyPostStatus status,
                                                 int maxMembers, Pageable pageable) {
        NativeQuery query = buildQuery(keyword, techStack, status, maxMembers, pageable);
        SearchHits<PostDocument> hits = elasticsearchOperations.search(query, PostDocument.class);
        List<StudyPostSummaryResponse> results = hits.stream()
                .map(SearchHit::getContent)
                .map(StudyPostSummaryResponse::fromDocument)
                .toList();
        return new PageImpl<>(results, pageable, hits.getTotalHits());
    }

    private NativeQuery buildQuery(String keyword, String techStack, StudyPostStatus status, int maxMembers, Pageable pageable) {
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

    public void index(PostDocument document) {
        postSearchRepository.save(document);
    }

    public void indexAll(List<PostDocument> documents) {
        postSearchRepository.saveAll(documents);
    }

    public void delete(String id) {
        postSearchRepository.deleteById(id);
    }
}
