package com.study.platform.domain.post.document

import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.post.model.StudyPostStatus
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.*
import java.time.LocalDateTime

@Document(indexName = "study_posts")
@Setting(settingPath = "elasticsearch/post-settings.json")
class PostDocument(
    @Id
    val id: String,

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    val title: String,

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    val description: String,

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "nori_analyzer"),
        otherFields = [InnerField(suffix = "keyword", type = FieldType.Keyword)]
    )
    val techStack: String,

    @Field(type = FieldType.Keyword)
    var status: String,

    @Field(type = FieldType.Integer)
    val maxMembers: Int,

    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second_millis])
    val deadline: LocalDateTime,

    @Field(type = FieldType.Keyword)
    val authorNickname: String,

    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second_millis])
    val createdAt: LocalDateTime
) {

    fun updateStatus(status: StudyPostStatus) {
        this.status = status.name
    }

    companion object {
        fun from(post: StudyPost): PostDocument = PostDocument(
            id = post.id.toString(),
            title = post.title,
            description = post.description,
            techStack = post.techStack ?: "",
            status = post.status.name,
            maxMembers = post.maxMembers,
            deadline = post.deadline!!,
            authorNickname = post.author!!.nickname,
            createdAt = post.createdAt!!
        )
    }
}
