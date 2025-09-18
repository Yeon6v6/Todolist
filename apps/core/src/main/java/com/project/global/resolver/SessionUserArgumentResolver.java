package com.project.global.resolver;

import com.project.auth.SessionUser;
import com.project.auth.dto.AuthUserDto;
import com.project.auth.jwt.JwtTokenProvider;
import com.project.utils.UserUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Objects;

public class SessionUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final JwtTokenProvider jwtTokenProvider;

    public SessionUserArgumentResolver(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(SessionUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String token = jwtTokenProvider.resolveToken(request);

        boolean validToken = false;
        try {
            validToken = jwtTokenProvider.validToken(token);
        } catch(ExpiredJwtException ignored) {}

        if (validToken) {
            SessionUser SessionUser = UserUtil.getSessionUser();
            if (SessionUser != null) return SessionUser;

            AuthUserDto.Authentication authUser = (AuthUserDto.Authentication) jwtTokenProvider.getAuthentication(token).getPrincipal();

            return UserUtil.buildSessionUser(authUser);
        } else {
            return SessionUser.builder()
                    .userId("temporary_user")
                    .build();
        }
    }
}
