package com.project.user.service;

import com.github.f4b6a3.ulid.UlidCreator;
import com.project.global.exception.BadRequestException;
import com.project.global.http.response.BaseResponseCode;
import com.project.user.dto.UserDto;
import com.project.user.entity.User;
import com.project.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto.RegisterResponse registerUser(UserDto.RegisterRequest request) {
        // 중복 검사
        validateUserRegistration(request);

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 사용자 생성
        User user = User.builder()
                .userId(UlidCreator.getMonotonicUlid().toString())
                .loginId(request.getLoginId())
                .password(encodedPassword)
                .userName(request.getUserName())
                .email(request.getEmail())
                .isNew(true)
                .build();

        User savedUser = userRepository.save(user);

        return new UserDto.RegisterResponse(
                savedUser.getLoginId(),
                savedUser.getUserName(),
                savedUser.getEmail()
        );
    }

    private void validateUserRegistration(UserDto.RegisterRequest request) {
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new BadRequestException(BaseResponseCode.DUP_LOGIN_ID,
                "이미 존재하는 사용자 ID입니다: " + request.getLoginId());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(BaseResponseCode.DUP_EMAIL,
                "이미 존재하는 이메일입니다: " + request.getEmail());
        }
    }

    @Transactional(readOnly = true)
    public boolean checkLoginIdExists(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    @Transactional(readOnly = true)
    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}