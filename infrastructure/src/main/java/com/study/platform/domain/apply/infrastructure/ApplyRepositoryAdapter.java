package com.study.platform.domain.apply.infrastructure;

import com.study.platform.domain.apply.model.Apply;
import com.study.platform.domain.apply.model.ApplyRepository;
import com.study.platform.domain.apply.model.ApplyStatus;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ApplyRepositoryAdapter implements ApplyRepository {

    private final ApplyJpaRepository applyJpaRepository;
    private final ApplyMapper applyMapper;

    @Override
    public Apply save(Apply apply) {
        try {
            ApplyJpaEntity entity = applyMapper.toEntity(apply);
            return applyMapper.toDomain(applyJpaRepository.save(entity));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.ALREADY_APPLIED);
        }
    }

    @Override
    public void delete(Apply apply) {
        applyJpaRepository.deleteById(apply.getId());
    }

    @Override
    public Optional<Apply> findById(UUID id) {
        return applyJpaRepository.findById(id)
                .map(applyMapper::toDomain);
    }

    @Override
    public boolean existsByPostIdAndApplicantId(UUID postId, UUID applicantId) {
        return applyJpaRepository.existsByPostIdAndApplicantId(postId, applicantId);
    }

    @Override
    public Optional<Apply> findByIdWithPostAndApplicant(UUID applyId) {
        return applyJpaRepository.findByIdWithPostAndApplicant(applyId)
                .map(applyMapper::toDomain);
    }

    @Override
    public Optional<Apply> findByPostIdAndApplicantId(UUID postId, UUID applicantId) {
        return applyJpaRepository.findByPostIdAndApplicantId(postId, applicantId)
                .map(applyMapper::toDomain);
    }

    @Override
    public long countByPostIdAndStatus(UUID postId, ApplyStatus status) {
        return applyJpaRepository.countByPostIdAndStatus(postId, status);
    }

    @Override
    public Optional<Apply> findByIdWithPostAndApplicantForUpdate(UUID applyId) {
        return applyJpaRepository.findByIdWithPostAndApplicantForUpdate(applyId)
                .map(applyMapper::toDomain);
    }
}
