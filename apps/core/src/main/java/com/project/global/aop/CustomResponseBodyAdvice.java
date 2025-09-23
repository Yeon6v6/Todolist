package com.project.global.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.global.http.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 클라이언트에게 데이터 넘기기 직전에 BaseResponse로 감싸기
 */
@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class CustomResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 컨트롤러 : @RestController
        boolean isRestController = returnType.getContainingClass().isAnnotationPresent(RestController.class);

        // 패키지 : 'com.project' package
        boolean isInSpecificPackage = returnType.getContainingClass().getPackage().getName().startsWith("com.project");

        // AuthController는 제외 (String 반환을 그대로 유지)
        boolean isAuthController = returnType.getContainingClass().getSimpleName().equals("AuthController");

        // 추가로 패키지 경로로도 확인
        boolean isAuthControllerByPackage = returnType.getContainingClass().getName().contains("auth.controller.AuthController");

        String className = returnType.getContainingClass().getSimpleName();
        String methodName = returnType.getMethod().getName();

        boolean shouldExclude = isAuthController || isAuthControllerByPackage;

        return isRestController && isInSpecificPackage && !shouldExclude;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        String className = returnType.getContainingClass().getSimpleName();
        String methodName = returnType.getMethod().getName();

        if (body instanceof BaseResponse || body instanceof UrlResource || body == null) {
            return body;
        }

        if (body instanceof String || isPrimitiveOrWrapper(body)) {
            BaseResponse<Object> baseResponse = new BaseResponse<>(body);
            try {
                if (body instanceof String) {
                    return objectMapper.writeValueAsString(baseResponse);
                }
                return baseResponse;
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing JSON", e);
            }
        }

        // Jpa Page객체 처리
        if (body instanceof org.springframework.data.domain.Page<?> page) {
            return new BaseResponse<>(page.getContent());
        }

        return new BaseResponse<>(body);
    }

    private boolean isPrimitiveOrWrapper(Object obj) {
        return obj instanceof Boolean || obj instanceof Byte || obj instanceof Character ||
                obj instanceof Short || obj instanceof Integer || obj instanceof Long ||
                obj instanceof Float || obj instanceof Double;
    }

}


