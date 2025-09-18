package com.project.global.http.response;

import lombok.Data;

/**
 * API 응답 표준
 */
@Data
public class BaseResponse<T> {
    private T data;
    private BaseResponseCode responseCode;
    private String responseMessage = "";

    public BaseResponse(T data) {
        this.data = data;
        this.responseCode = BaseResponseCode.SUCCESS;
    }

    public BaseResponse(BaseResponseCode responseCode, String responseMessage) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }

    public BaseResponse(T data, BaseResponseCode responseCode, String responseMessage) {
        this.data = data;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }
}
