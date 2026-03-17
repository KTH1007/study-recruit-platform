package com.study.platform.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.util.UUID;

@Getter
@MappedSuperclass
public class BaseEntity extends BaseTimeEntity {

    @CreatedBy
    @Column(updatable = false, columnDefinition = "BINARY(16)")
    private UUID createdBy;

    @LastModifiedBy
    @Column(columnDefinition = "BINARY(16)")
    private UUID updatedBy;
}
