package com.study.platform.support.fake

import com.study.platform.global.event.DomainEventPublisher

class FakeDomainEventPublisher : DomainEventPublisher {

    private val events: MutableList<Any> = ArrayList()

    override fun publish(event: Any) {
        events.add(event)
    }

    fun getEvents(): List<Any> = events

    fun <T> getEventsOf(type: Class<T>): List<T> =
        events.filterIsInstance(type)

    fun hasEventOf(type: Class<*>): Boolean =
        events.any { type.isInstance(it) }
}
