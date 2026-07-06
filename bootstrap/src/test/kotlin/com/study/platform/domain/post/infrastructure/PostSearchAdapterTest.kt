package com.study.platform.domain.post.infrastructure

import com.study.platform.domain.post.document.PostDocument
import com.study.platform.domain.post.document.PostSearchRepository
import com.study.platform.domain.post.model.StudyPostStatus
import com.study.platform.support.AbstractContainerSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.utility.DockerImageName
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class PostSearchAdapterTest : AbstractContainerSupport() {

    @Autowired
    private lateinit var postSearchAdapter: PostSearchAdapter

    @Autowired
    private lateinit var postSearchRepository: PostSearchRepository

    @Autowired
    private lateinit var elasticsearchOperations: ElasticsearchOperations

    companion object {
        private val elasticsearchImage = ImageFromDockerfile()
            .withDockerfileFromBuilder { builder ->
                builder
                    .from("docker.elastic.co/elasticsearch/elasticsearch:9.0.2")
                    .run("elasticsearch-plugin install --batch analysis-nori")
                    .build()
            }

        val elasticsearch: ElasticsearchContainer = ElasticsearchContainer(
            DockerImageName.parse(elasticsearchImage.get())
                .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch")
        ).apply {
            withEnv("xpack.security.enabled", "false")
        }

        init {
            elasticsearch.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun overrideElasticsearchProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.elasticsearch.uris") { "http://${elasticsearch.httpHostAddress}" }
        }
    }

    @AfterEach
    fun tearDown() {
        postSearchRepository.deleteAll()
    }

    private fun indexDocument(title: String, techStack: String): PostDocument {
        val doc = PostDocument(
            id = UUID.randomUUID().toString(),
            title = title,
            description = "테스트 설명",
            techStack = techStack,
            status = StudyPostStatus.OPEN.name,
            maxMembers = 5,
            deadline = LocalDateTime.now().plusDays(7),
            authorNickname = "작성자",
            createdAt = LocalDateTime.now()
        )
        postSearchRepository.save(doc)
        elasticsearchOperations.indexOps(PostDocument::class.java).refresh()
        return doc
    }

    @Test
    fun `nori_형태소분석으로_활용형_검색어가_원문을_찾는다`() {
        // given
        indexDocument("자바 스터디원 모집합니다", "Java")

        // when
        val results = postSearchAdapter.search("스터디", null, null, 0, PageRequest.of(0, 10))

        // then
        assertThat(results.content.map { it.title }).contains("자바 스터디원 모집합니다")
    }

    @Test
    fun `동의어_필터로_영단어_검색어가_한글_문서를_찾는다`() {
        // given
        indexDocument("스프링 부트 스터디", "Spring")

        // when
        val results = postSearchAdapter.search("spring", null, null, 0, PageRequest.of(0, 10))

        // then
        assertThat(results.content.map { it.title }).contains("스프링 부트 스터디")
    }
}
