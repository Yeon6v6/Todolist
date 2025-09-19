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

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final List<String> permitUrlArr;
    private final SecurityContextRepository securityContextRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, List<String> permitUrlArr, SecurityContextRepository securityContextRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.permitUrlArr = permitUrlArr;
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String jwt = jwtTokenProvider.resolveToken(request);

        try {
            if (jwtTokenProvider.validToken(jwt) && jwtTokenProvider.verifyTokenPair(jwt, request)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                SecurityContext context = SecurityContextHolder.getContext();
                securityContextRepository.saveContext(context, request, response);

                // 확장 속성추가(request)
                AuthUserDto.Authentication loginAuthUser = UserUtil.getLoginAuthUser();
                if (loginAuthUser != null) {
                    loginAuthUser.getExtendProperty().put("request", request);
                    UserUtil.buildSessionUser(loginAuthUser);
                }
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            AntPathMatcher antPathMatcher = new AntPathMatcher();
            if (permitUrlArr.stream().anyMatch(pattern -> antPathMatcher.match(pattern, request.getRequestURI()))) {
                filterChain.doFilter(request, response);
                return;
            }

            BaseResponseCode responseCode = BaseResponseCode.ACCESS_TOKEN_EXPIRED;
            BaseResponse<String> baseResponse = new BaseResponse<>(responseCode, responseCode.getMessage());

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(baseResponse);

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setStatus(401);

            try (PrintWriter writer = response.getWriter()) {
                writer.print(jsonResponse);
                writer.flush();
            }
        } finally {
            // 요청 처리 후 인증 정보 정리
            SecurityContextHolder.clearContext();
        }
    }
}
