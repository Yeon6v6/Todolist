package com.project.auth.repository;

import com.project.auth.entity.AuthLoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthLoginHistoryRepository extends JpaRepository<AuthLoginHistory, String> {

    List<AuthLoginHistory> findByUserIdOrderByLoginDateDesc(String userId);

    List<AuthLoginHistory> findByUserIdAndLoginDateBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<AuthLoginHistory> findByTokenId(String tokenId);

    long countByUserIdAndLoginDateAfter(String userId, LocalDateTime loginDate);
}