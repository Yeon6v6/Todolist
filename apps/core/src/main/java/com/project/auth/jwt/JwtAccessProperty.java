package com.project.auth.jwt;

import com.project.auth.jwt.config.AccessRule;
import com.project.auth.jwt.config.TokenConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@ConfigurationProperties("security.jwt")
public class JwtAccessProperty {
    private String secret;
    private String roleName;
    private Map<String, TokenConfig> validSeconds;
    private List<AccessRule> accessRules;
    private String permitUrl;

    public long getAccessTokenTTL() {
        return getAccessTokenTTL("todolist");
    }

    public long getRefreshTokenTTL() {
        return getRefreshTokenTTL("todolist");
    }

    public long getAccessTokenTTL(String tokenType) {
        return Optional.ofNullable(validSeconds.get(tokenType))
                .map(TokenConfig::accessToken)
                .orElse(1800L); // 기본값 30분
    }

    public long getRefreshTokenTTL(String tokenType) {
        return Optional.ofNullable(validSeconds.get(tokenType))
                .map(TokenConfig::refreshToken)
                .orElse(7200L); // 기본값 2시간
    }

    public List<AccessRule> getAccessRules() {
        return Optional.ofNullable(accessRules).orElse(List.of());
    }

    public List<List<String>> getAccess() {
        return getAccessRules().stream()
                .map(rule -> {
                    List<String> result = new java.util.ArrayList<>();
                    result.add(rule.pattern());
                    result.addAll(rule.roles());
                    return result;
                })
                .toList();
    }
}