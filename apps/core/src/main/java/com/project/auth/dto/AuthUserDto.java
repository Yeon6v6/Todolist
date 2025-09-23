package com.project.auth.dto;

import com.project.auth.AuthConstants;
import com.project.common.CommonConstants;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

public class AuthUserDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Request {
        @NotNull(message = "Authentication Failed")
        private String id;
        @NotNull(message = "Authentication Failed")
        private String password;
        @Builder.Default
        private String locale = CommonConstants.COMM_LOCAL_KO;
        @Builder.Default
        private String loginMode = AuthConstants.LOGIN_BYID;
        @Builder.Default
        private String device = CommonConstants.DEVICE_PC;
        private String deviceId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RequestDupUser {
        @NotNull
        private String name;
        @Builder.Default
        private String locale = CommonConstants.COMM_LOCAL_KO;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Authentication {
        private String userId;
        private String loginId;
        private String device;
        private String deviceId;
        @Builder.Default
        private String tokenType = AuthConstants.TOKEN_TYPE_APPLICATION; // application, external
        private String tokenId;
        @Builder.Default
        private Map<String, Object> extendProperty = new HashMap<>();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LoginResponse {
        private String accessToken;
        private String tokenId;
        private String userId;
        private String loginId;
    }
}
