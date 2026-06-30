package com.study.platform.support

import com.study.platform.domain.apply.model.Apply
import com.study.platform.domain.comment.model.Comment
import com.study.platform.domain.notification.model.Notification
import com.study.platform.domain.notification.model.NotificationType
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.team.model.StudyTeam
import com.study.platform.domain.team.model.TeamMember
import com.study.platform.domain.team.model.TeamSchedule
import com.study.platform.domain.user.model.User
import com.study.platform.support.fake.FakeDomainEventPublisher
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.UUID

object TestFixtures {

    fun createUser(
        id: UUID = UUID.randomUUID(),
        kakaoId: String = "kakao-${id}",
        nickname: String = "user-${id.toString().take(8)}",
        email: String = "${id.toString().take(8)}@test.com"
    ): User = User.create(kakaoId, nickname, email).also {
        ReflectionTestUtils.setField(it, "id", id)
    }

    fun createStudyPost(
        id: UUID = UUID.randomUUID(),
        author: User = createUser(),
        title: String = "스터디 모집",
        description: String = "열심히 합니다",
        techStack: String? = "Kotlin",
        maxMembers: Int = 5,
        deadline: LocalDateTime = LocalDateTime.now().plusDays(7)
    ): StudyPost = StudyPost.create(author, title, description, techStack, maxMembers, deadline).also {
        ReflectionTestUtils.setField(it, "id", id)
    }

    fun createApply(
        id: UUID = UUID.randomUUID(),
        post: StudyPost = createStudyPost(),
        applicant: User = createUser(),
        message: String = "지원합니다"
    ): Apply = Apply.create(post, applicant, message, FakeDomainEventPublisher()).also {
        ReflectionTestUtils.setField(it, "id", id)
    }

    fun createComment(
        id: UUID = UUID.randomUUID(),
        post: StudyPost = createStudyPost(),
        author: User = createUser(),
        content: String = "댓글입니다"
    ): Comment = Comment.create(post, author, content, FakeDomainEventPublisher()).also {
        ReflectionTestUtils.setField(it, "id", id)
    }

    fun createNotification(
        id: UUID = UUID.randomUUID(),
        receiver: User = createUser(),
        type: NotificationType = NotificationType.APPLY_RECEIVED,
        message: String = "알림입니다",
        targetId: UUID? = null
    ): Notification = Notification.create(receiver, type, message, targetId).also {
        ReflectionTestUtils.setField(it, "id", id)
    }

    fun createStudyTeam(
        id: UUID = UUID.randomUUID(),
        post: StudyPost = createStudyPost()
    ): StudyTeam = StudyTeam.create(post).also {
        ReflectionTestUtils.setField(it, "id", id)
    }

    fun createLeaderMember(
        id: UUID = UUID.randomUUID(),
        team: StudyTeam = createStudyTeam(),
        user: User = createUser()
    ): TeamMember = TeamMember.createLeader(team, user).also {
        ReflectionTestUtils.setField(it, "id", id)
    }

    fun createNormalMember(
        id: UUID = UUID.randomUUID(),
        team: StudyTeam = createStudyTeam(),
        user: User = createUser()
    ): TeamMember = TeamMember.createMember(team, user).also {
        ReflectionTestUtils.setField(it, "id", id)
    }

    fun createTeamSchedule(
        id: UUID = UUID.randomUUID(),
        team: StudyTeam = createStudyTeam(),
        title: String = "스터디 일정",
        description: String? = null,
        scheduledAt: LocalDateTime = LocalDateTime.now().plusDays(1)
    ): TeamSchedule = TeamSchedule.create(team, title, description, scheduledAt).also {
        ReflectionTestUtils.setField(it, "id", id)
    }
}
