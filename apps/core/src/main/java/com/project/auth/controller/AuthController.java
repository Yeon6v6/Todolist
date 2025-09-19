package com.project.auth.controller;

import com.project.auth.dto.AuthUserDto;
import com.project.auth.service.AuthService;
import com.project.utils.AESUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/core/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final AESUtil aesUtil;

    @PostMapping("/login")
    public String login(@Valid @RequestBody AuthUserDto.Request request) throws Exception {
        String password = request.getPassword();
        String decrypt = aesUtil.decrypt(password);

        request.setPassword(decrypt);

        return authService.login(request);
    }
}