package com.project.global.aop;

import com.project.global.exception.AuthenticationException;
import com.project.global.exception.BaseException;
import com.project.global.exception.NotFoundException;
import com.project.global.http.response.BaseResponse;
import com.project.global.http.response.BaseResponseCode;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.project.global.exception.BadRequestException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * 컨트롤러에서 오류가 날경우 예외클래스 기준으로 클라이언트에게 상태코드 및 메시지 전달
 */
@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class BaseControllerAdvice {
    private final MessageSource messageSource;


    @ExceptionHandler(value = {BaseException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public BaseResponse<?> handleBaseError(HttpServletRequest request, BaseException e) {
        request.setAttribute("exception", e); // ExceptionHandlerInterceptor 에 전달
        log.error("Controller BaseException occurred: {}", e.getMessage(), e);
        return new BaseResponse<String>(e.getResponseCode(), e.getMessage());
    }

    @ExceptionHandler(value = {NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public BaseResponse<?> handleNotFoundError(HttpServletRequest request, NotFoundException e) {
        request.setAttribute("exception", e); // ExceptionHandlerInterceptor 에 전달
        log.error("Controller NotFoundException occurred: {}", e.getMessage(), e);
        return new BaseResponse<>(e.getResponseCode(), e.getMessage());
    }

    @ExceptionHandler(value = {BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public BaseResponse<?> handleBadRequestError(HttpServletRequest request, BadRequestException e) {
        request.setAttribute("exception", e); // ExceptionHandlerInterceptor 에 전달
        log.error("Controller BadRequestException occurred: {}", e.getMessage(), e);
        return new BaseResponse<>(e.getParam(), e.getResponseCode(), e.getMessage());
    }

    @ExceptionHandler(value = {AuthenticationException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public BaseResponse<?> handleAuthenticationError(AuthenticationException e) {
        log.error("Controller AuthenticationException occurred: {}", e.toString());
        return new BaseResponse<>(e.getParam(), e.getResponseCode(), e.getMessage());
    }

    @ExceptionHandler(value = {ExpiredJwtException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public BaseResponse<?> handleTokenExpireError(ExpiredJwtException e) {
        // controller 에서 해당 오류를 catch 하는 경우는 refresh token 만료시 (세션 만료)
        // 이경우 별도 로그를 남기지는 않음
        BaseResponseCode code = BaseResponseCode.REFRESH_TOKEN_EXPIRED;
        return new BaseResponse<>(code, code.getMessage());
    }

    @ExceptionHandler(value = {
            TypeMismatchException.class,
            IllegalArgumentException.class,
            MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class,
            NoSuchElementException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public BaseResponse<?> handleBadRequest(HttpServletRequest request, Exception ex) {
        request.setAttribute("exception", ex); // ExceptionHandlerInterceptor 에 전달
        log.error("Controller Parameter exception occurred: {}", ex.getMessage(), ex);

        String message;

        if (ex instanceof MethodArgumentNotValidException) {
            message = processMethodArgumentNotValidException((MethodArgumentNotValidException) ex);
        } else if (ex instanceof MissingServletRequestParameterException) {
            message = processMissingServletRequestParameterException((MissingServletRequestParameterException) ex);
        } else {
            message = messageSource.getMessage(BaseResponseCode.BAD_REQUEST.name(), null, null);
        }

        return new BaseResponse<>(BaseResponseCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(value = {AsyncRequestTimeoutException.class})
    public void handleTimeoutError(HttpServletResponse response, AsyncRequestTimeoutException e) {
        // SSE timeout exception 은 패스
        if ("text/event-stream".equals(response.getContentType())) return;

        log.error("Error handling AsyncRequestTimeoutException", e);
    }

    @ExceptionHandler(value = {org.springframework.web.context.request.async.AsyncRequestNotUsableException.class})
    @ResponseStatus(HttpStatus.OK) // This is a client disconnection, not a server error
    @ResponseBody
    public void handleAsyncRequestNotUsable(HttpServletRequest request, 
                                      org.springframework.web.context.request.async.AsyncRequestNotUsableException ex) {
        // Log concisely without stack trace for client disconnections
        log.info("SSE client disconnected: {}", ex.getMessage());
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public BaseResponse<?> handleInternalError(HttpServletRequest request, Exception ex) {
        request.setAttribute("exception", ex); // ExceptionHandlerInterceptor 에 전달
        log.error("Controller Exception occurred: {}", ex.getMessage(), ex);

        BaseResponseCode code = BaseResponseCode.INTERNAL_SERVER_ERROR;
        String message = messageSource.getMessage(code.name(), null, null);

        return new BaseResponse<>(code, message);
    }

    /*
     * Request Dto에 @NotNull을 명시한 경우 필드오류 표시
     */
    private String processMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        StringBuilder messageBuilder = new StringBuilder();

        for (FieldError fieldError : fieldErrors) {
            String defaultMessage = fieldError.getDefaultMessage();

            // 기본 메시지가 기본적인 Bean Validation 메시지인 경우 커스텀 메시지로 대체
            if (isDefaultBeanValidationMessage(defaultMessage)) {
                defaultMessage = String.format("The field '%s' must not be null", fieldError.getField());
            }

            messageBuilder.append(defaultMessage).append("; ");
        }

        return messageBuilder.toString().trim();
    }

    private boolean isDefaultBeanValidationMessage(String message) {
        return "널이어서는 안됩니다".equals(message);
    }

    private String processMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return ex.getParameterName() + " parameter is missing";
    }
}
