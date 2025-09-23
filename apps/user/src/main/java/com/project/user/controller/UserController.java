package com.project.user.controller;

import com.project.user.dto.UserDto;
import com.project.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserDto.RegisterResponse registerUser(@Valid @RequestBody UserDto.RegisterRequest request) {
        return userService.registerUser(request);
    }

    @GetMapping("/check/loginid/{loginId}")
    public Boolean checkLoginIdExists(@PathVariable String loginId) {
        return !userService.checkLoginIdExists(loginId);
    }

    @GetMapping("/check/email/{email}")
    public Boolean checkEmailExists(@PathVariable String email) {
        return !userService.checkEmailExists(email);
    }
}