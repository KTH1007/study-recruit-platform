package com.study.platform.global.event

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringDomainEventPublisherAdapter(
    private val applicationEventPublisher: ApplicationEventPublisher
) : DomainEventPublisher {

    override fun publish(event: Any) {
        applicationEventPublisher.publishEvent(event)
    }
}
