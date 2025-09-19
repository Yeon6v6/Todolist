package com.project.auth.config;

import com.project.auth.jwt.JwtAccessProperty;
import com.project.auth.jwt.JwtAuthenticationEntryPoint;
import com.project.auth.jwt.JwtAuthenticationFilter;
import com.project.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * spring-security 인증 방식 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class AuthConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessProperty jwtAccessProperty;

    @Value("${security.permit-url}")
    private String[] permitUrlArr;
    @Value("${security.permit-dev-url}")
    private String[] permitDevUrlArr;
    @Value("${security.permit-swagger-url}")
    private String[] permitSwaggerUrlArr;
    @Value("${security.permit-resource-url}")
    private String[] permitResourceUrlArr;

    static {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        List<String> permitUrls = new ArrayList<>();
        permitUrls.addAll(List.of(permitUrlArr)); // permitUrlArr에 있는 각 URL에 대해 인증 없이 허용
        permitUrls.addAll(List.of(permitDevUrlArr)); // permitDevUrlArr에 있는 각 URL에 대해 인증 없이 허용
        permitUrls.addAll(List.of(permitSwaggerUrlArr)); // permitSwaggerUrlArr에 있는 각 URL에 대해 인증 없이 허용
        permitUrls.addAll(List.of(permitResourceUrlArr)); // 에디터관련 리소스URL에 대해 인증 없이 허용

        String schemeAndHostAndPort = "";

        final String finalSchemeAndHostAndPort = schemeAndHostAndPort;

        return http
                .headers((header) ->
                                header
                                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)) // XSS 보안
                                        .contentSecurityPolicy(cps -> cps.policyDirectives("script-src 'self' " + finalSchemeAndHostAndPort + " 'unsafe-inline' 'unsafe-eval'"))
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(authorize -> {
                    for (String url : permitUrls) {
                        // 토큰 체크없이 바로 통과시켜줄 url
                        authorize.requestMatchers(url).permitAll();
                    }

                    for (List<String> access : jwtAccessProperty.getAccess()) {
                        // 특정 Role 에 따라 허용해줄 url
                        String url = access.getFirst();
                        String[] roles = access.subList(1, access.size()).toArray(String[]::new);
                        authorize.requestMatchers(url).hasAnyRole(roles);
                    }

                    // 나머지 모든 요청은 무시
                    authorize.anyRequest().denyAll();
                })
                .exceptionHandling(handling ->
                        handling.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                .addFilterBefore(corsFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, permitUrls, securityContextRepository()), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    public DelegatingSecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new HttpSessionSecurityContextRepository(),
                new RequestAttributeSecurityContextRepository());
    }
}
