package com.project.auth.dto;

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
    @Builder
    public static class Request {
        @NotNull(message = "Authentication Failed")
        private String id;
        @NotNull(message = "Authentication Failed")
        private String password;
        @Builder.Default
        private String locale = CommonConstants.COMM_LOCAL_KO;
        @Builder.Default
        private String loginMode = "byId";
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
        private String tokenType = "application"; // application, external
        @Builder.Default
        private Map<String, Object> extendProperty = new HashMap<>();
    }
}
