package com.project.global.exception;

import com.project.global.http.response.BaseResponseCode;
import java.util.Map;

public class AuthenticationException extends BaseException {

    public AuthenticationException(BaseResponseCode responseCode) {
        super(responseCode);
    }

    public AuthenticationException(BaseResponseCode responseCode, Map<String, Object> param) {
        super(responseCode, param);
    }
}
