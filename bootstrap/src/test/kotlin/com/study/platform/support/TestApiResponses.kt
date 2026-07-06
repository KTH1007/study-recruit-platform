package com.study.platform.support

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class TestApiResponse<T> @JsonCreator constructor(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("data") val data: T?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TestPage<T> @JsonCreator constructor(
    @JsonProperty("content") val content: List<T>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TestPostResponse @JsonCreator constructor(
    @JsonProperty("id") val id: UUID,
    @JsonProperty("title") val title: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TestApplyResponse @JsonCreator constructor(
    @JsonProperty("id") val id: UUID,
    @JsonProperty("applicantNickname") val applicantNickname: String,
    @JsonProperty("status") val status: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TestTeamResponse @JsonCreator constructor(
    @JsonProperty("id") val id: UUID
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TestTeamMemberResponse @JsonCreator constructor(
    @JsonProperty("nickname") val nickname: String
)
