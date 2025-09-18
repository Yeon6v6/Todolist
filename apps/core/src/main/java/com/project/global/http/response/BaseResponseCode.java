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
    ACCESS_TOKEN_EXPIRED("access_token 만료"),

    ;

    private final String message;

    BaseResponseCode(String message) {
        this.message = message;
    }
}
