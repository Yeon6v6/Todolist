package com.project.auth.service;

import com.project.auth.dto.AuthUserDto;
import com.project.auth.entity.AuthUser;

public interface AuthInterface {
    // todo: 로그인 인증 완료 직후 추가 인증 및 추가 정책 검증 등
    void postLogin(AuthUserDto.Request authRequest, AuthUser authUser);

    // todo: 전체 로그인 프로세스 완료 후 추가적으로 처리해야 할 후처리
    void postLoginAfter(AuthUserDto.Request authRequest, AuthUserDto.Authentication userAuthentication, AuthUser authUser);
}
