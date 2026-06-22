package com.study.platform.domain.post.application

import com.study.platform.domain.post.dto.response.StudyPostResponse
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.post.usecase.FindStudyPostUseCase
import com.study.platform.global.constant.CacheConstants
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class FindStudyPostService(
    private val studyPostRepository: StudyPostRepository
) : FindStudyPostUseCase {

    @Cacheable(cacheNames = [CacheConstants.POST_CACHE], key = "#postId")
    override fun execute(postId: UUID): StudyPostResponse {
        val post = studyPostRepository.findByIdWithAuthor(postId)
            
            ?: throw CustomException(ErrorCode.POST_NOT_FOUND)
        return StudyPostResponse.from(post)
    }
}
