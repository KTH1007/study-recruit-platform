package com.study.platform.support.fake;

import com.study.platform.domain.apply.model.Apply;
import com.study.platform.domain.apply.model.ApplyRepository;
import com.study.platform.domain.apply.model.ApplyStatus;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

public class FakeApplyRepository implements ApplyRepository {

    private final Map<UUID, Apply> store = new HashMap<>();

    @Override
    public Apply save(Apply apply) {
        // JPA @GeneratedValue 역할 - ID가 없으면 직접 부여
        if (apply.getId() == null) {
            ReflectionTestUtils.setField(apply, "id", UUID.randomUUID());
        }
        // 유니크 제약 검사 (post_id + applicant_id)
        boolean duplicate = store.values().stream()
                .filter(a -> !a.getId().equals(apply.getId()))
                .anyMatch(a ->
                        a.getPost().getId().equals(apply.getPost().getId()) &&
                                a.getApplicant().getId().equals(apply.getApplicant().getId())
                );
        if (duplicate) {
            throw new CustomException(ErrorCode.ALREADY_APPLIED);
        }
        store.put(apply.getId(), apply);
        return apply;
    }

    @Override
    public void delete(Apply apply) {
        store.remove(apply.getId());
    }

    @Override
    public Optional<Apply> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean existsByPostIdAndApplicantId(UUID postId, UUID applicantId) {
        return store.values().stream()
                .anyMatch(a ->
                        a.getPost().getId().equals(postId) &&
                                a.getApplicant().getId().equals(applicantId)
                );
    }

    public List<Apply> findAllByPostId(UUID postId) {
        return store.values().stream()
                .filter(a -> a.getPost().getId().equals(postId))
                .toList();
    }

    @Override
    public Optional<Apply> findByIdWithPostAndApplicant(UUID applyId) {
        return Optional.ofNullable(store.get(applyId));
    }

    @Override
    public Optional<Apply> findByPostIdAndApplicantId(UUID postId, UUID applicantId) {
        return store.values().stream()
                .filter(a ->
                        a.getPost().getId().equals(postId) &&
                                a.getApplicant().getId().equals(applicantId)
                )
                .findFirst();
    }

    @Override
    public long countByPostIdAndStatus(UUID postId, ApplyStatus status) {
        return store.values().stream()
                .filter(a ->
                        a.getPost().getId().equals(postId) &&
                                a.getStatus() == status
                )
                .count();
    }

    @Override
    public Optional<Apply> findByIdWithPostAndApplicantForUpdate(UUID applyId) {
        // 인메모리라 락 불필요, 동일하게 조회
        return Optional.ofNullable(store.get(applyId));
    }
}
