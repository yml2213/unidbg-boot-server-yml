package com.anjia.unidbgserver.exception;

import com.anjia.unidbgserver.response.Result;
import com.anjia.unidbgserver.response.enums.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    /**
     * 未知异常
     */
    @ExceptionHandler(value = Exception.class)
    public Result systemExceptionHandler(Exception e) {
        log.error("system exception！The reason is：{}", e.getMessage(), e);
        return Result.fail(ErrorCodeEnum.UNKNOWN);
    }

    /**
     * validation参数校验异常
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<String> methodArgumentNotValidExceptionExceptionHandler(MethodArgumentNotValidException e) {
        StringBuilder errorMsg = new StringBuilder();
        e.getBindingResult().getFieldErrors().forEach(x -> errorMsg.append(x.getDefaultMessage()).append(","));
        String message = errorMsg.toString();
        log.info("validation parameters error！The reason is:{}", message);
        return Result.fail(ErrorCodeEnum.BAD_REQUEST.getCode(), message.substring(0, message.length() - 1));
    }


    /**
     * 自定义校验异常（如参数校验等）
     */
    @ExceptionHandler(value = BusinessException.class)
    public Result<ErrorCodeEnum> businessExceptionHandler(BusinessException e) {
        log.info("business exception！The reason is：{}", e.getMessage(), e);
        return Result.fail(e.getErrorCode(), e.getMessage());
    }
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public Result<ErrorCodeEnum> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        return Result.fail(ErrorCodeEnum.BAD_REQUEST.getCode(),ErrorCodeEnum.BAD_REQUEST.getMessage());
    }

    /**
     * http请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleException(HttpRequestMethodNotSupportedException e) {
        log.error(e.getMessage(), e);
        return Result.fail(-1, String.format("不支持'%s'请求", e.getMethod()));
    }






}
