package com.study.platform.domain.post.model

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface PostSearchPort {

    fun search(keyword: String?, techStack: String?, status: StudyPostStatus?, maxMembers: Int, pageable: Pageable): Page<PostSearchResult>
    fun index(post: StudyPost)
    fun indexAll(posts: List<StudyPost>)
    fun delete(postId: UUID)
}
