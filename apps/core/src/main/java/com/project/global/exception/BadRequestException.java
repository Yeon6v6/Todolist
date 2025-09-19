package com.project.global.exception;

import com.project.global.http.response.BaseResponseCode;

import java.util.Map;

/**
 * BadRequest 오류처리
 */
public class BadRequestException extends BaseException {

    public BadRequestException() {
        super(BaseResponseCode.BAD_REQUEST);
    }

    public BadRequestException(BaseResponseCode responseCode) {
        super(responseCode);
    }

    public BadRequestException(String message) {
        super(BaseResponseCode.BAD_REQUEST, message);
    }

    public BadRequestException(BaseResponseCode responseCode, String message) {
        super(responseCode, message);
    }

    public BadRequestException(BaseResponseCode responseCode, Map<String, Object> param) {
        super(responseCode, param);
    }
}
