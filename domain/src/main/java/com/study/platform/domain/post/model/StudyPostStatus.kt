package com.study.platform.domain.post.model

enum class StudyPostStatus(val description: String) {
    OPEN("모집 중"),
    CLOSED("모집 마감"),
    FULL("정원 초과")
}
