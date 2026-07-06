package com.study.platform.support.fake

import org.springframework.kafka.support.Acknowledgment

class FakeAcknowledgment : Acknowledgment {

    private var acknowledged = false

    override fun acknowledge() {
        acknowledged = true
    }

    fun isAcknowledged(): Boolean = acknowledged
}
