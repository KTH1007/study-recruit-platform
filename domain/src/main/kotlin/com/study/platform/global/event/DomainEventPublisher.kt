package com.study.platform.global.event

interface DomainEventPublisher {
    fun publish(event: Any)
}
