package com.project.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.auth.dto.AuthUserDto;
import com.project.global.http.response.BaseResponse;
import com.project.global.http.response.BaseResponseCode;
import com.project.utils.UserUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityContextRepository securityContextRepository;
    private final List<String> permitUrls;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, SecurityContextRepository securityContextRepository, List<String> permitUrls) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.securityContextRepository = securityContextRepository;
        this.permitUrls = permitUrls;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        // permit all url인 경우 필터 실행x => 바로 다음 필터로
        String requestUri = request.getRequestURI();
        if(permitUrls.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestUri))) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = jwtTokenProvider.resolveToken(request);
        if (StringUtils.isEmpty(jwt)) {
            // 토큰이 없는 경우 다음 필터로 진행
            filterChain.doFilter(request, response);
            return;
        }
        try {
            if (jwtTokenProvider.validToken(jwt) && jwtTokenProvider.verifyTokenPair(jwt, request)) {
                // JWT 토큰이 유효한 경우
                Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // SecurityContext를 Repository에 저장하여 Spring Security와 연결
                SecurityContext context = SecurityContextHolder.getContext();
                securityContextRepository.saveContext(context, request, response);

                log.debug("JWT Authentication successful for user: {}", authentication.getName());

                // 사용자 정보 확장 처리
                AuthUserDto.Authentication loginAuthUser = UserUtil.getLoginAuthUser();
                if (loginAuthUser != null) {
                    loginAuthUser.getExtendProperty().put("request", request);
                    UserUtil.buildSessionUser(loginAuthUser);
                }
            } else {
                log.debug("JWT token validation failed");
            }
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            // 토큰 만료 시 401 응답 반환
            BaseResponseCode responseCode = BaseResponseCode.ACCESS_TOKEN_EXPIRED;
            BaseResponse<String> baseResponse = new BaseResponse<>(responseCode, responseCode.getMessage());

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(baseResponse);

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            try (PrintWriter writer = response.getWriter()) {
                writer.print(jsonResponse);
                writer.flush();
            }
            // 필터 체인 중단
            return;
        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage());
            // 다른 JWT 관련 오류는 로그만 남기고 계속 진행
        }
        filterChain.doFilter(request, response);
    }
}