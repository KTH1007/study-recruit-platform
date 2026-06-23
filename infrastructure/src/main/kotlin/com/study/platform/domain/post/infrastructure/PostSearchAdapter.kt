package com.study.platform.domain.post.infrastructure

import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.*
import com.study.platform.domain.post.document.PostDocument
import com.study.platform.domain.post.document.PostSearchRepository
import com.study.platform.domain.post.model.PostSearchPort
import com.study.platform.domain.post.model.PostSearchResult
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.post.model.StudyPostStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PostSearchAdapter(
    private val elasticsearchOperations: ElasticsearchOperations,
    private val postSearchRepository: PostSearchRepository
) : PostSearchPort {

    override fun search(
        keyword: String?,
        techStack: String?,
        status: StudyPostStatus?,
        maxMembers: Int,
        pageable: Pageable
    ): Page<PostSearchResult> {
        val query = buildQuery(keyword, techStack, status, maxMembers, pageable)
        val hits = elasticsearchOperations.search(query, PostDocument::class.java)
        val results = hits.map { it: SearchHit<PostDocument> -> toResult(it.content) }.toList()
        return PageImpl(results, pageable, hits.totalHits)
    }

    override fun index(post: StudyPost) {
        postSearchRepository.save(PostDocument.from(post))
    }

    override fun indexAll(posts: List<StudyPost>) {
        postSearchRepository.saveAll(posts.map { PostDocument.from(it) })
    }

    override fun delete(postId: UUID) {
        postSearchRepository.deleteById(postId.toString())
    }

    private fun toResult(doc: PostDocument): PostSearchResult = PostSearchResult(
        UUID.fromString(doc.id),
        doc.authorNickname,
        doc.title,
        doc.techStack,
        doc.maxMembers,
        doc.deadline,
        StudyPostStatus.valueOf(doc.status),
        doc.createdAt
    )

    private fun buildQuery(
        keyword: String?,
        techStack: String?,
        status: StudyPostStatus?,
        maxMembers: Int,
        pageable: Pageable
    ): NativeQuery {
        val boolQuery = BoolQuery.Builder()

        if (!keyword.isNullOrBlank()) {
            boolQuery.must(
                MultiMatchQuery.of { m ->
                    m.fields("title", "description")
                        .query(keyword)
                        .type(TextQueryType.BestFields)
                }._toQuery()
            )
        }

        if (!techStack.isNullOrBlank()) {
            boolQuery.filter(
                MatchQuery.of { m ->
                    m.field("techStack").query(techStack)
                }._toQuery()
            )
        }

        if (status != null) {
            boolQuery.filter(
                TermQuery.of { t ->
                    t.field("status").value(status.name)
                }._toQuery()
            )
        }

        if (maxMembers > 0) {
            boolQuery.filter(
                RangeQuery.of { r ->
                    r.number { n -> n.field("maxMembers").lte(maxMembers.toDouble()) }
                }._toQuery()
            )
        }

        return NativeQuery.builder()
            .withQuery(boolQuery.build()._toQuery())
            .withSort { s -> s.score { sc -> sc.order(SortOrder.Desc) } }
            .withSort { s -> s.field { f -> f.field("createdAt").order(SortOrder.Desc) } }
            .withPageable(pageable)
            .build()
    }
}
