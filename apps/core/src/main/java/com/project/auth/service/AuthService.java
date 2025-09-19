package com.project.auth.service;

import com.project.auth.dto.AuthUserDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Transactional
    public String login(AuthUserDto.Request authRequest) {
        /*User user = findUser(authRequest);

        try {
            authenticationManagerBuilder.getObject().authenticate(new UsernamePasswordAuthenticationToken(user.getUserId(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            authFailCounterManager.incrementLoginFailCount(user.getUserId());
            throw new AuthenticationException(BaseResponseCode.BAD_CREDENTIALS);
        }

        // 인증된 사용자 사용 가능여부 체크
        validLoginAuth(user, authRequest);

        // 모듈별 추가적으로 처리할 부분 처리
        UserAuth userAuthInfo = userAuthService.getUserAuthInfo(user.getUserId(), user.getCompanyId(), null);
        loginInterface.ifPresent(impl -> impl.postLogin(authRequest, userAuthInfo));

        // 토큰 발급
        AuthUserDto.Authentication authentication = generateAuthentication(authRequest, user);
        String token = generateToken(authentication);

        // login after
        loginAfter(authentication, userAuthInfo);

        // 모듈별 추가적으로 처리할 부분 처리
        loginInterface.ifPresent(impl -> impl.postLoginAfter(authRequest, authentication, user));

        return token;*/

        return null;
    }

}
