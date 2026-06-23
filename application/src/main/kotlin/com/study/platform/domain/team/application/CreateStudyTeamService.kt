package com.study.platform.domain.team.application

import com.study.platform.domain.apply.event.ApplyApprovedEvent
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.team.model.StudyTeam
import com.study.platform.domain.team.model.StudyTeamRepository
import com.study.platform.domain.team.model.TeamMember
import com.study.platform.domain.team.model.TeamMemberRepository
import com.study.platform.domain.team.usecase.CreateStudyTeamUseCase
import com.study.platform.domain.user.model.UserRepository
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class CreateStudyTeamService(
    private val studyTeamRepository: StudyTeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val studyPostRepository: StudyPostRepository,
    private val userRepository: UserRepository
) : CreateStudyTeamUseCase {

    private val log = LoggerFactory.getLogger(CreateStudyTeamService::class.java)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun execute(event: ApplyApprovedEvent) {
        try {
            if (studyTeamRepository.findByPostId(event.postId) != null) {
                addMember(event)
                return
            }

            val post = studyPostRepository.findByIdWithAuthor(event.postId)
                ?: throw CustomException(ErrorCode.POST_NOT_FOUND)
            val team = studyTeamRepository.save(StudyTeam.create(post))

            teamMemberRepository.save(TeamMember.createLeader(team, post.author!!))

            val applicant = userRepository.findById(event.applicantId)
                ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
            teamMemberRepository.save(TeamMember.createMember(team, applicant))
        } catch (e: DataIntegrityViolationException) {
            log.warn("팀 생성 중복 감지 (동시성) - 멤버 추가로 전환. postId: {}", event.postId)
            addMember(event)
        }
    }

    private fun addMember(event: ApplyApprovedEvent) {
        val team = studyTeamRepository.findByPostId(event.postId)
            ?: throw CustomException(ErrorCode.TEAM_NOT_FOUND)
        if (teamMemberRepository.existsByTeamIdAndUserId(team.id!!, event.applicantId)) {
            return
        }
        val applicant = userRepository.findById(event.applicantId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        teamMemberRepository.save(TeamMember.createMember(team, applicant))
    }
}
