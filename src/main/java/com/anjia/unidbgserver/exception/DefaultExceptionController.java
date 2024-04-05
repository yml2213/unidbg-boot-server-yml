package com.anjia.unidbgserver.exception;

import com.anjia.unidbgserver.response.Result;
import com.anjia.unidbgserver.response.enums.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@Slf4j
public class DefaultExceptionController extends AbstractErrorController {
    private static final String ERROR_PATH = "/error";

    public DefaultExceptionController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }


    @RequestMapping(value = ERROR_PATH)
    public Result<String> error(HttpServletRequest request) {
        WebRequest webRequest = new ServletWebRequest(request);
        Throwable e = getError(webRequest);
        if (e == null) {
            Map<String, Object> attributes = getErrorAttributes(request, ErrorAttributeOptions.defaults());
            Object timestamp = attributes.get("timestamp");
            Object status = attributes.get("status");
            String error = attributes.get("error").toString();
            Object path = attributes.get("path");
            log.error("status {} error {} path{} timestamp {}", status, error, path, timestamp);
            return Result.fail(Integer.parseInt(status.toString()), error);
        }

        return Result.fail(ErrorCodeEnum.INTERNAL_SERVER_ERROR.getCode(), ErrorCodeEnum.INTERNAL_SERVER_ERROR.getMessage());
    }

    private Throwable getError(WebRequest webRequest) {
        return (Throwable) this.getAttribute(webRequest, "javax.servlet.error.exception");
    }

    private Object getAttribute(RequestAttributes requestAttributes, String name) {
        return requestAttributes.getAttribute(name, 0);
    }
}
