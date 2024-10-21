package com.dtaquito_micro_services.sport_service.shared.domain.model.entities;


import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class AuditableModel {

    @Getter
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Long createdAt;

    @Getter
    @LastModifiedDate
    @Column(nullable = false)
    private Long updatedAt;
}
