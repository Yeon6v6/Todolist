package com.project.auth.config;

import com.project.auth.jwt.JwtAccessProperty;
import com.project.auth.jwt.JwtAuthenticationFilter;
import com.project.auth.jwt.JwtTokenProvider;
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
import org.springframework.web.filter.CorsFilter;
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        String permitUrlStr = jwtAccessProperty.getPermitUrl();
        List<String> permitUrls = permitUrlStr != null ?
                Arrays.asList(permitUrlStr.split(",")) :
                Arrays.asList("/error", "/actuator/**");

        log.info("JWT Filter Permit URLs: {}", permitUrls);
        return new JwtAuthenticationFilter(jwtTokenProvider, securityContextRepository(), permitUrls);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/").permitAll()
                                .requestMatchers("/error").permitAll()
                                .requestMatchers("/actuator/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/user/register").permitAll()
                                .requestMatchers("/core/auth/login").permitAll()
                                .requestMatchers("/user/check/**").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .httpBasic(httpBasic -> {})
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
}