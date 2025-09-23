package com.project.auth.service;

import com.project.auth.AuthConstants;
import com.project.auth.dto.AuthUserDto;
import com.project.auth.dto.mapping.AuthUserDtoMapping;
import com.project.auth.entity.AuthLoginHistory;
import com.project.auth.entity.AuthUser;
import com.project.auth.jwt.JwtTokenProvider;
import com.project.auth.repository.AuthLoginHistoryRepository;
import com.project.auth.repository.AuthRepository;
import com.project.global.exception.AuthenticationException;
import com.project.global.http.response.BaseResponseCode;
import com.project.utils.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthRepository authRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final Optional<AuthInterface> loginInterface;
    private final AuthLoginHistoryRepository authLoginHistoryRepository;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public String login(AuthUserDto.Request authRequest) {
        System.out.println("Login attempt for user: " + authRequest.getId());
        List<AuthUser> authUsers = findUser(authRequest);

        if(authUsers.isEmpty()) {
            System.out.println("User not found: " + authRequest.getId());
            throw new AuthenticationException(BaseResponseCode.BAD_CREDENTIALS);
        }
        if(authUsers.size() > 1) {
            System.out.println("Multiple users found: " + authRequest.getId());
            throw new AuthenticationException(BaseResponseCode.DUP_USER);
        }

        AuthUser authUser = authUsers.getFirst();
        System.out.println("Found user: " + authUser.getLoginId());

        // 비밀번호 검증
        try {
            System.out.println("Authenticating user: " + authUser.getLoginId() + " with password: " + authRequest.getPassword());
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authUser.getLoginId(), authRequest.getPassword()));
            System.out.println("Authentication successful");
        } catch (BadCredentialsException e) {
            System.out.println("Authentication failed: " + e.getMessage());
            throw new AuthenticationException(BaseResponseCode.BAD_CREDENTIALS);
        }

        // todo: 로그인 사용자의 사용 가능여부 체크
//        validLoginAuth(authUser, authRequest);

        // todo: 모듈별 추가적으로 처리
//        loginInterface.ifPresent(impl -> impl.postLogin(authRequest, authUser));

        // 토큰 발급
        AuthUserDto.Authentication authentication = AuthUserDtoMapping.INSTANCE.toAuthentication(authUser, authRequest.getDevice(), authRequest.getDeviceId());
        String token = generateToken(authentication);

        // login 이후 처리
        loginAfter(authentication);

        // todo: 모듈별 추가적으로 처리
//        loginInterface.ifPresent(impl -> impl.postLoginAfter(authRequest, authentication, authUser));

        return token;
    }

    public List<AuthUser> findUser(AuthUserDto.Request authRequest) {
        if(AuthConstants.LOGIN_BYID.equals(authRequest.getLoginMode())) {
            return authRepository.findByLoginId(authRequest.getId())
                    .map(List::of)
                    .orElse(Collections.emptyList());
        }else{
            return authRepository.findByUserName(authRequest.getId());
        }
    }

    protected String generateToken(AuthUserDto.Authentication user) {
            return generateAccessAndRefreshTokens(user);
    }

    protected String generateAccessAndRefreshTokens(AuthUserDto.Authentication user) {
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        jwtTokenProvider.setRefreshToken(refreshToken, user.getTokenType());

        return accessToken;
    }

    private void loginAfter(AuthUserDto.Authentication authentication) {
        // todo : 추가 작업 기술
        
        // 로그인 정보 및 이력 저장
        String remoteAddr = AuthUtil.getClientIp();
        saveLoginInfoAsync(authentication, remoteAddr);
    }

    @Async("loginTaskExecutor")
    public CompletableFuture<Void> saveLoginInfoAsync(AuthUserDto.Authentication authentication, String remoteAddr) {
        AuthLoginHistory authLoginHistory = AuthLoginHistory.builder()
                .tokenId(authentication.getTokenId())
                .userId(authentication.getUserId())
                .loginIp(remoteAddr)
                .loginDate(LocalDateTime.now())
                .device(authentication.getDevice())
                .deviceId(authentication.getDeviceId())
                .build();

        authLoginHistoryRepository.save(authLoginHistory);

        return CompletableFuture.completedFuture(null);
    }

}
