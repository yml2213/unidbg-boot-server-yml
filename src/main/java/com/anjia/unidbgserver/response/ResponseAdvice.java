package com.anjia.unidbgserver.response;

import cn.hutool.core.date.DateUtil;
import com.anjia.unidbgserver.response.enums.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.LinkedHashMap;

/**
 * @Author: cym
 * @create: 2023-03-27 00:08
 * @description:
 * @version: 1.0
 **/

@RestControllerAdvice
@Slf4j
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class converterType) {
        return true;
    }


    @Override
    public Object beforeBodyWrite(Object body, @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType, @NonNull Class selectedConverterType,
                                  @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {

        if (body instanceof Result) {
            return ((Result<?>) body).setTimestamp(DateUtil.now());
        }
        //自定义基本异常
        else if (body instanceof LinkedHashMap) {
            return body;
        } else if (body instanceof String) {
            return body;
        }
        // 如果返回类型不是 Result，表示出现异常
        Result<Object> result = Result.fail(null);
        result.setCode(ErrorCodeEnum.UNKNOWN.getCode());
        result.setMessage(ErrorCodeEnum.UNKNOWN.getMessage());
        result.setTimestamp(DateUtil.now());

        return result;
    }

}
