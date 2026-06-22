package com.study.platform.domain.post.document

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface PostSearchRepository : ElasticsearchRepository<PostDocument, String>
