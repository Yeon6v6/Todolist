package com.project.user.controller;

import com.project.global.http.response.BaseResponse;
import com.project.global.http.response.BaseResponseCode;
import com.project.user.dto.UserDto;
import com.project.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<UserDto.RegisterResponse>> registerUser(@Valid @RequestBody UserDto.RegisterRequest request) {
        try {
            UserDto.RegisterResponse response = userService.registerUser(request);
            return ResponseEntity.ok(new BaseResponse<>(response));
        } catch (IllegalArgumentException e) {
            log.warn("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new BaseResponse<>(BaseResponseCode.BAD_REQUEST, e.getMessage()));
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>(BaseResponseCode.INTERNAL_SERVER_ERROR, "회원가입 처리 중 오류가 발생했습니다"));
        }
    }

    @GetMapping("/check/loginid/{loginId}")
    public ResponseEntity<BaseResponse<Boolean>> checkLoginIdExists(@PathVariable String loginId) {
        boolean exists = userService.checkLoginIdExists(loginId);
        return ResponseEntity.ok(new BaseResponse<>(!exists));
    }

    @GetMapping("/check/email/{email}")
    public ResponseEntity<BaseResponse<Boolean>> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.checkEmailExists(email);
        return ResponseEntity.ok(new BaseResponse<>(!exists));
    }
}