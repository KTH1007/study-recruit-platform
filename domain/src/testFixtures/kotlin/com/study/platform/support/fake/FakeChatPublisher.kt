package com.study.platform.support.fake

import com.study.platform.domain.chat.model.ChatMessage
import com.study.platform.domain.chat.model.ChatPublisher

class FakeChatPublisher : ChatPublisher {

    private val published: MutableList<ChatMessage> = ArrayList()

    override fun publish(message: ChatMessage) {
        published.add(message)
    }

    fun getPublished(): List<ChatMessage> = published
}
