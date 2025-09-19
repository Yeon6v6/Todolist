package com.project.auth.jwt.config;

public record TokenConfig(
        Long accessToken,
        Long refreshToken
) {}