package com.project.auth.repository;

import com.project.auth.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<AuthUser, String> {

    Optional<AuthUser> findByLoginId(String loginId);

    List<AuthUser> findByUserName(String userName);

    Optional<AuthUser> findByEmail(String email);
}
