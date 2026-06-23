package com.study.platform.domain.apply.model

enum class ApplyStatus(val description: String) {
    PENDING("검토중"),
    APPROVED("승인"),
    REJECTED("거절")
}
