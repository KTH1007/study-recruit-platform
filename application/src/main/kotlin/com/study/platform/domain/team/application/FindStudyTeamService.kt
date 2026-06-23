package com.study.platform.domain.team.application

import com.study.platform.domain.team.dto.response.StudyTeamResponse
import com.study.platform.domain.team.model.StudyTeamRepository
import com.study.platform.domain.team.usecase.FindStudyTeamUseCase
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class FindStudyTeamService(
    private val studyTeamRepository: StudyTeamRepository
) : FindStudyTeamUseCase {

    override fun execute(teamId: UUID): StudyTeamResponse {
        val team = studyTeamRepository.findById(teamId)
            ?: throw CustomException(ErrorCode.TEAM_NOT_FOUND)
        return StudyTeamResponse.from(team)
    }

    override fun executeByPostId(postId: UUID): StudyTeamResponse {
        val team = studyTeamRepository.findByPostId(postId)
            ?: throw CustomException(ErrorCode.TEAM_NOT_FOUND)
        return StudyTeamResponse.from(team)
    }
}
