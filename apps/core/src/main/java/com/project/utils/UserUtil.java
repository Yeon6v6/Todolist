package com.project.utils;

import com.project.auth.SessionUser;
import com.project.auth.dto.AuthUserDto;
import com.project.auth.dto.mapping.AuthUserDtoMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserUtil {

    public static SessionUser getSessionUser() {
        AuthUserDto.Authentication loginAuthUser = getLoginAuthUser();
        if (loginAuthUser != null) {
            SessionUser sessionUser = (SessionUser)loginAuthUser.getExtendProperty().get("sessionUser   ");
            if (sessionUser != null) {
                return sessionUser;
            }
        }
        return null;
    }

    public static AuthUserDto.Authentication getLoginAuthUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() != null) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof AuthUserDto.Authentication) {
                    return (AuthUserDto.Authentication) principal;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static SessionUser buildSessionUser(AuthUserDto.Authentication authUser) {
        // 추가적으로 SessionUser에 값 세팅 할 경우 method에  param 넘기기
        SessionUser sessionUser = AuthUserDtoMapping.INSTANCE.toSessionUser(authUser);
        authUser.getExtendProperty().put("sessionUser", sessionUser);
        
        return sessionUser;
    }
}
