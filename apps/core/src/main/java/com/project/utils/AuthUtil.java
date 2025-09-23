package com.project.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

public class AuthUtil {
    private static final String[] IP_HEADERS = {
            "X-Forwarded-For", "x-forwarded-for", "Proxy-Client-IP",
            "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR",
            "X-Real-IP", "X-RealIP"
    };

    public static String getClientIp() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        return Arrays.stream(IP_HEADERS)
                .map(request::getHeader)
                .filter(ip->isValidIp(ip))
                .findFirst()
                .orElse(request.getRemoteAddr());
    }

    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}
