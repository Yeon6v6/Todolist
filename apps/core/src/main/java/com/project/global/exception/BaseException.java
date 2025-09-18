package com.project.global.exception;

import com.project.global.http.response.BaseResponseCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * API 응답오류 예외처리
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class BaseException extends RuntimeException {
    private BaseResponseCode responseCode;
    private String message; // 메시지 전달용
    private Map<String, Object> param; // 추가 전달 할 값에 대한 파라미터

    public BaseException(BaseResponseCode responseCode) {
        this.responseCode = responseCode;
        this.message = responseCode.getMessage();
    }

    public BaseException(BaseResponseCode responseCode, String message) {
        this.responseCode = responseCode;
        this.message = message;
    }

    public BaseException(BaseResponseCode responseCode, Map<String, Object> param) {
        this.responseCode = responseCode;
        this.message = responseCode.getMessage();
        this.param = param;
    }
}
