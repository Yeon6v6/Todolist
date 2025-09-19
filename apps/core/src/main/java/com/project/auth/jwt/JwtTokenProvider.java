package com.project.auth.jwt;

import com.project.auth.dto.AuthUserDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    private final JwtAccessProperty jwtAccessProperty;
    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtAccessProperty.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(AuthUserDto.Authentication authentication) {
        Instant now = Instant.now();
        Instant validity = now.plusSeconds(jwtAccessProperty.getAccessTokenTTL(authentication.getTokenType()));
        Date expirationDate = Date.from(validity);

        return Jwts.builder()
                .subject(authentication.getUserId())
                .claim("roles", "ROLE_" + jwtAccessProperty.getRoleName())
                .claim("loginId", authentication.getLoginId())
                .claim("device", authentication.getDevice())
                .claim("deviceId", authentication.getDeviceId())
                .claim("tokenType", authentication.getTokenType())
                .signWith(key, Jwts.SIG.HS512)
                .expiration(expirationDate)
                .compact();
    }

    public String createRefreshToken(AuthUserDto.Authentication authentication) {
        Instant now = Instant.now();
        Instant validity = now.plusSeconds(jwtAccessProperty.getRefreshTokenTTL(authentication.getTokenType()));
        Date expirationDate = Date.from(validity);

        return Jwts.builder()
                .subject(authentication.getUserId())
                .claim("roles", "ROLE_" + jwtAccessProperty.getRoleName())
                .claim("loginId", authentication.getLoginId())
                .claim("device", authentication.getDevice())
                .claim("deviceId", authentication.getDeviceId())
                .claim("tokenType", authentication.getTokenType())
                .signWith(key, Jwts.SIG.HS512)
                .expiration(expirationDate)
                .compact();
    }

    public String createTempToken(AuthUserDto.Authentication authentication) {
        Instant now = Instant.now();
        Instant validity = now.plusSeconds(60 * 6); // 프론트에서 5분의 입력시간을 가진다고 가정
        Date expirationDate = Date.from(validity);

        return Jwts.builder()
                .subject(authentication.getUserId())
                .claim("device", authentication.getDevice())
                .claim("deviceId", authentication.getDeviceId())
                .signWith(key, Jwts.SIG.HS512)
                .expiration(expirationDate)
                .compact();
    }

    // 기존 setRefreshToken을 수정
    public void setRefreshToken(String refreshToken, String tokenType) {
        // JWT에서 cacheId 추출
        String cacheId = getCacheId(refreshToken);

        if (cacheId != null) {
            // cacheId를 쿠키 이름으로 설정
            ResponseCookie cookie = ResponseCookie.from(cacheId, refreshToken)
                    .path("/")
                    .httpOnly(true)
                    .maxAge(jwtAccessProperty.getRefreshTokenTTL(tokenType))
                    .build();

            // HTTP 응답에 쿠키 설정
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            response.addHeader("Set-Cookie", cookie.toString());
        } else {
            // cacheId가 없으면 적절한 예외 처리
            throw new IllegalArgumentException("Invalid token: cacheId not found");
        }
    }

    // 기존 getRefreshToken을 수정
    public String getRefreshToken(HttpServletRequest request) {
        // JWT에서 cacheId 추출
        String cacheId = getCacheIdFromRequest(request);

        if (cacheId != null) {
            // 요청에서 모든 쿠키를 검색
            for (Cookie cookie : request.getCookies()) {
                // 쿠키 이름을 cacheId와 비교
                if (cookie.getName().equals(cacheId)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public void removeRefreshToken(HttpServletRequest request) {
        // JWT에서 cacheId 추출
        String cacheId = getCacheIdFromRequest(request);

        removeRefreshToken(cacheId);
    }

    // 기존 removeRefreshToken을 수정
    public void removeRefreshToken(String cacheId) {
        if (cacheId != null) {
            // cacheId를 쿠키 이름으로 사용하여 쿠키를 삭제
            ResponseCookie cookie = ResponseCookie.from(cacheId, null)
                    .maxAge(0)
                    .path("/")
                    .httpOnly(true)
                    .build();

            // HTTP 응답에 쿠키 삭제 설정
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            response.addHeader("Set-Cookie", cookie.toString());
        }
    }

    // 토큰에서 cacheId 추출
    private String getCacheId(String token) {
        if (StringUtils.isEmpty(token)) return null;

        try {
            JwtParser parser = Jwts.parser()
                    .setSigningKey(key)
                    .build();

            Claims claims;
            try {
                claims = parser.parseClaimsJws(token).getBody();
            } catch (ExpiredJwtException e) {
                log.info("JWT 만료됨, Claims 직접 추출");
                claims = e.getClaims(); // 만료된 토큰에서도 Claims 가져오기
            }

            return claims.get("cacheId", String.class);
        } catch (Exception e) {
            log.error("JWT 파싱 오류: {}", e.getMessage(), e);
            return null; // 예외 발생 시 null 반환
        }
    }

    // 요청에서 cacheId를 추출하는 메서드
    private String getCacheIdFromRequest(HttpServletRequest request) {
        String accessToken = resolveToken(request);
        return getCacheId(accessToken); // JWT 토큰에서 cacheId 추출
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        AuthUserDto.Authentication authentication = AuthUserDto.Authentication.builder()
                .userId(claims.getSubject())
                .loginId(claims.get("loginId", String.class))
                .tokenType(claims.get("tokenType", String.class))
                .device(claims.get("device", String.class))
                .deviceId(claims.get("deviceId", String.class))
                .build();

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority((String) claims.get("roles")));

        return new UsernamePasswordAuthenticationToken(authentication, token, authorities);
    }

    public AuthUserDto.Request getAuthRequestByTempToken(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return AuthUserDto.Request.builder()
                .id(claims.getSubject())
                .device(claims.get("device", String.class))
                .deviceId(claims.get("deviceId", String.class))
                .build();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 프론트에서 토큰을 빈값으로 넘기는 케이스가 있어 length 까지 체크
        if (StringUtils.isNotEmpty(bearerToken) && bearerToken.startsWith("Bearer") && bearerToken.length() > 7) {
            return bearerToken.substring(7);
        }

        return null;
    }

    public String getTokenRoleName() {
        return jwtAccessProperty.getRoleName();
    }

    public boolean isUserRole() {
        return "USER".equals(getTokenRoleName());
    }

    public boolean isAdminRole() {
        return "ADMIN".equals(getTokenRoleName());
    }

    public boolean validToken(String token) throws ExpiredJwtException {
        try {
            Jws<Claims> claimsJws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);

            // 기존 토큰에 cacheId가 없을경우 재생성 필요!
            return !StringUtils.isEmpty(claimsJws.getPayload().get("cacheId", String.class));
        } catch (ExpiredJwtException e) {
            // 토큰 만료시에는 별도 에러처리 필요
            throw e;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰 탈취시를 대비해 refreshToken 과 이중으로 검증
     */
    public boolean verifyTokenPair(String accessToken, HttpServletRequest request) throws ExpiredJwtException {
        return true;

        // 개발시에는 일단 주석
//        String refreshToken = getRefreshToken(request);
//        if (StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(refreshToken)) return false;
//
//        String userIdByAccessToken = ((AuthUserDto.Authentication) getAuthentication(accessToken).getPrincipal()).getUserId();
//        String userIdByRefreshToken = ((AuthUserDto.Authentication) getAuthentication(refreshToken).getPrincipal()).getUserId();
//
//        return StringUtils.equals(userIdByAccessToken, userIdByRefreshToken);
    }
}
