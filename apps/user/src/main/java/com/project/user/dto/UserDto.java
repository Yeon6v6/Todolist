package com.project.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class UserDto {

    @Data
    public static class RegisterRequest {
        @NotBlank
        @Size(min = 4, max = 20)
        private String loginId;

        @NotBlank
        @Size(min = 8, max = 100)
        private String password;

        @NotBlank
        @Size(max = 50)
        private String userName;

        @NotBlank
        @Email
        private String email;
    }

    @Data
    public static class RegisterResponse {
        private String loginId;
        private String userName;
        private String email;

        public RegisterResponse() {}

        public RegisterResponse(String loginId, String userName, String email) {
            this.loginId = loginId;
            this.userName = userName;
            this.email = email;
        }
    }
}