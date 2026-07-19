package com.study.platform.global.kafka

import org.slf4j.Logger
import org.springframework.kafka.support.Acknowledgment
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.TimeUnit

class DltRetryHandler<T : Any>(
    private val objectMapper: ObjectMapper,
    private val kafkaMessagePublisher: KafkaMessagePublisher,
    private val log: Logger,
    private val eventClass: Class<T>,
    private val retryTopic: String,
    private val maxRetry: Int,
    private val getRetryCount: (T) -> Int,
    private val withIncrementedRetry: (T) -> T,
    private val describe: (T) -> String,
    private val onPermanentFailure: (T, String) -> Unit
) {
    fun handle(payload: String, ack: Acknowledgment, exceptionMessage: String?) {
        try {
            val event = objectMapper.readValue(payload, eventClass)
            val retryCount = getRetryCount(event)
            log.error("DLT 수신 - {}, retryCount={}, 원인={}", describe(event), retryCount, exceptionMessage)

            if (retryCount < maxRetry) {
                try {
                    kafkaMessagePublisher.publish(retryTopic, objectMapper.writeValueAsString(withIncrementedRetry(event)))
                        .get(5, TimeUnit.SECONDS)
                    log.info("{} 토픽 재투입 - retryCount={}", retryTopic, retryCount + 1)
                } catch (e: Exception) {
                    // 재투입 자체가 실패하면 이벤트를 잃어버리지 않도록 영구 저장한다.
                    log.error("{} 토픽 재투입 실패 - DB 영구 저장. {}", retryTopic, describe(event), e)
                    onPermanentFailure(event, "재투입 실패: ${e.message}")
                }
            } else {
                onPermanentFailure(event, exceptionMessage ?: "unknown")
                log.error("최대 재시도 초과 - DB 영구 저장. {}", describe(event))
            }
        } catch (e: Exception) {
            log.error("DLT 페이로드 파싱 실패 - payload: {}", payload, e)
        } finally {
            ack.acknowledge()
        }
    }
}
