package com.study.platform.domain.post.application

import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.domain.post.model.FailedPostSync
import com.study.platform.domain.post.model.FailedPostSyncRepository
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.kafka.DltRetryHandler
import com.study.platform.global.kafka.KafkaMessagePublisher
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Component
class PostSyncDltConsumer(
    objectMapper: ObjectMapper,
    kafkaMessagePublisher: KafkaMessagePublisher,
    failedPostSyncRepository: FailedPostSyncRepository
) {
    private val handler = DltRetryHandler(
        objectMapper = objectMapper,
        kafkaMessagePublisher = kafkaMessagePublisher,
        log = LoggerFactory.getLogger(PostSyncDltConsumer::class.java)!!,
        eventClass = PostSyncEvent::class.java,
        retryTopic = KafkaConstants.POST_SYNC_TOPIC,
        maxRetry = KafkaConstants.MAX_DLT_RETRY,
        getRetryCount = { it.retryCount },
        withIncrementedRetry = { it.withRetry() },
        describe = { "postId=${it.postId}, type=${it.operationType}" },
        onPermanentFailure = { event, reason -> failedPostSyncRepository.save(FailedPostSync.from(event, reason)) }
    )

    @Transactional
    @KafkaListener(topics = [KafkaConstants.POST_SYNC_DLT_TOPIC], groupId = KafkaConstants.POST_SYNC_DLT_GROUP)
    fun consume(
        payload: String,
        ack: Acknowledgment,
        @Header(name = KafkaHeaders.EXCEPTION_MESSAGE, required = false) exceptionMessage: String?
    ) = handler.handle(payload, ack, exceptionMessage)
}
