package com.project.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuthUser implements Persistable<String> {

    @Id
    @Column(name = "userId", length = 50)
    private String userId;

    @Column(nullable = false)
    private String loginId;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String userName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Transient
    @Builder.Default
    private Boolean isNew = false;

    @Override
    public String getId() { return userId; }

    @Override
    public boolean isNew() {
        return userId == null || isNew;
    }

    @PrePersist
    public void generateId() {
        if (userId == null) {
            userId = com.github.f4b6a3.ulid.UlidCreator.getMonotonicUlid().toString();
        }
    }

    public enum UserStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}