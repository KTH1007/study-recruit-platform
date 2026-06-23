package com.study.platform.domain.post.application

import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.domain.post.event.PostSyncOperationType
import com.study.platform.domain.post.model.FailedPostSync
import com.study.platform.domain.post.model.FailedPostSyncRepository
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.global.constant.KafkaConstants
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class PostSyncKafkaConsumer(
    private val objectMapper: ObjectMapper,
    private val studyPostRepository: StudyPostRepository,
    private val postSearchService: PostSearchService,
    private val failedPostSyncRepository: FailedPostSyncRepository
) {
    private val log = LoggerFactory.getLogger(PostSyncKafkaConsumer::class.java)!!

    @KafkaListener(topics = [KafkaConstants.POST_SYNC_TOPIC], groupId = KafkaConstants.POST_SYNC_GROUP)
    fun consume(payload: String, ack: Acknowledgment) {
        val event = objectMapper.readValue(payload, PostSyncEvent::class.java)

        if (event.retryCount >= KafkaConstants.MAX_DLT_RETRY) {
            failedPostSyncRepository.save(FailedPostSync.from(event, "메인 Consumer 최종 실패"))
            ack.acknowledge()
            return
        }

        if (event.operationType == PostSyncOperationType.DELETE) {
            postSearchService.delete(event.postId.toString())
        } else {
            studyPostRepository.findByIdWithAuthor(event.postId)
                ?.let { postSearchService.index(it) }
        }

        ack.acknowledge()
        log.info("ES 동기화 완료 - postId: {}, type: {}", event.postId, event.operationType)
    }
}
