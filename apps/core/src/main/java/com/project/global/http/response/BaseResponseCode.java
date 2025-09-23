package com.project.global.http.response;

import lombok.Getter;

/**
 * API요청 응답코드관리
 */
@Getter
public enum BaseResponseCode {
    SUCCESS("SUCCESS"),
    ERROR("ERROR"),
    NOT_FOUND("NOT_FOUND"),
    BAD_REQUEST("BAD_REQUEST"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR"),

    UNAUTHORIZED("인증 실패"),
    ACCESS_TOKEN_EXPIRED("Access token 만료"),
    REFRESH_TOKEN_EXPIRED("Refresh token 만료"),
    BAD_CREDENTIALS("로그인 실패"),
    DUP_USER("중복 사용자가 존재"),
    DUP_LOGIN_ID("중복된 사용자 ID"),
    DUP_EMAIL("중복된 이메일")
    ;

    private final String message;

    BaseResponseCode(String message) {
        this.message = message;
    }
}
