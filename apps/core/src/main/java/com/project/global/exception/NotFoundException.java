package com.project.global.exception;

import com.project.global.http.response.BaseResponseCode;

/**
 * NotFound 오류처리
 */
public class NotFoundException extends BaseException {

    public NotFoundException() {
        super(BaseResponseCode.NOT_FOUND);
    }

    public NotFoundException(BaseResponseCode responseCode) {
        super(responseCode);
    }

    public NotFoundException(BaseResponseCode responseCode, String message) {
        super(responseCode, message);
    }
}
