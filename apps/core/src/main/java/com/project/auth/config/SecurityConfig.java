package com.project.auth.config;

import com.project.auth.jwt.JwtAccessProperty;
import com.project.auth.jwt.JwtAuthenticationFilter;
import com.project.auth.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.List;

/**
 * spring-security 인증 방식 설정
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtAccessProperty.class)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAccessProperty jwtAccessProperty;

    @Value("${spring.mvc.servlet.path}")
    private String servletPath;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String permitUrlStr = jwtAccessProperty.getPermitUrl();
        List<String> permitUrls = permitUrlStr != null ?
                Arrays.asList(permitUrlStr.split(",")) :
                Arrays.asList();

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> {
                    // 기본 허용 URL들
                    for (String url : permitUrls) {
                        String trimmedUrl = url.trim();
                        String requestPath = removeServletPath(trimmedUrl);
                        authorize.requestMatchers(requestPath).permitAll();
                    }

                    authorize.anyRequest().authenticated();
                })
                .addFilterBefore(createJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(httpBasic -> {})
                .build();
    }

    /**
     * servlet path를 제거하여 실제 요청 경로 반환
     * /api/v1/user/register → /user/register (servlet path가 /api/v1인 경우)
     */
    private String removeServletPath(String url) {
        if (servletPath != null && !servletPath.isEmpty() && url.startsWith(servletPath)) {
            return url.replace(servletPath, "");
        }
        return url;
    }

    private JwtAuthenticationFilter createJwtAuthenticationFilter() {
        String permitUrlStr = jwtAccessProperty.getPermitUrl();
        List<String> permitUrls = permitUrlStr != null ?
                Arrays.asList(permitUrlStr.split(",")) :
                Arrays.asList();

        log.info("JWT Filter Permit URLs: {}", permitUrls);
        return new JwtAuthenticationFilter(jwtTokenProvider, new HttpSessionSecurityContextRepository(), permitUrls);
    }

    private UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        source.registerCorsConfiguration("/**", config);
        return source;
    }
}