package com.project.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_loginhistory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuthLoginHistory implements Persistable<String> {

    @Id
    private String id;
    private String tokenId;
    private String userId;
    private String loginIp;
    private LocalDateTime loginDate;
    private LocalDateTime logoutDate;
    private String device;
    private String deviceId;

    @Builder.Default
    @Transient
    private Boolean isNew = false;

    @Override
    public String getId() {
        return tokenId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = com.github.f4b6a3.ulid.UlidCreator.getMonotonicUlid().toString();
        }
    }

}