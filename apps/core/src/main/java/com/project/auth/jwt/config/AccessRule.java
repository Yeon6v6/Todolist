package com.project.auth.jwt.config;

import java.util.List;

public record AccessRule(
        String pattern,
        List<String> roles
) {}