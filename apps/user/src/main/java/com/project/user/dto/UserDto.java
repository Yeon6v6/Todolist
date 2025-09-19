package com.project.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class UserDto {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "사용자 ID는 필수입니다")
        @Size(min = 4, max = 20, message = "사용자 ID는 4-20자 사이여야 합니다")
        private String loginId;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다")
        private String password;

        @NotBlank(message = "사용자명은 필수입니다")
        @Size(max = 50, message = "사용자명은 50자를 초과할 수 없습니다")
        private String userName;

        @NotBlank(message = "이메일은 필수입니다")
        @Email
        private String email;
    }

    @Data
    public static class RegisterResponse {
        private String userId;
        private String userName;
        private String email;
        private String message;

        public static RegisterResponse success(String userId, String userName, String email) {
            RegisterResponse response = new RegisterResponse();
            response.setUserId(userId);
            response.setUserName(userName);
            response.setEmail(email);
            response.setMessage("회원가입이 완료되었습니다");
            return response;
        }
    }
}