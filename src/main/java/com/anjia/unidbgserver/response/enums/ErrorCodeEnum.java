package com.anjia.unidbgserver.response.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 错误码枚举类
 *
 * @author cym
 */
@Getter
public enum ErrorCodeEnum {


  // 请求格式错误
  BAD_REQUEST(HttpStatus.BAD_REQUEST, 400, "请求参数错误，请检查格式或内容"),

  // 未授权
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 401, "未授权，需要身份验证或授权令牌无效"),

  // 权限不足
  FORBIDDEN(HttpStatus.FORBIDDEN, 403, "禁止访问，访问被拒绝"),

  // 未找到该请求
  NOT_FOUND(HttpStatus.NOT_FOUND, 404, "资源未找到，请求的URL或资源不存在"),

  // 方法不允许
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 405, "方法不被允许，请求方法无效"),

  //请求超时
  REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, 408, "请求超时，请求未在规定时间内完成"),

  // 不支持的媒体类型,
  UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, 415, "不支持的媒体类型"),


  // 请求过多
  TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, 429, "请求过多，请稍后再试"),

  // 服务器内部错误
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "服务器内部错误"),

  //网关错误
  BAD_GATEWAY(HttpStatus.BAD_GATEWAY, 502, "网关错误，网关服务器不能完成请求"),

  SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, 503, "服务不可用，服务器暂时不可用"),
  GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, 504, "网关超时，网关服务器未能及时响应请求"),

  // 未知错误
  UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR, -999, "未知错误");

  private final HttpStatus httpStatus;
  private final int code;
  private final String message;

  ErrorCodeEnum(HttpStatus httpStatus, int code, String message) {
    this.httpStatus = httpStatus;
    this.code = code;
    this.message = message;
  }

  public static ErrorCodeEnum getByCode(int code) {
    for (ErrorCodeEnum e : values()) {
      if (e.getCode() == code) {
        return e;
      }
    }
    return UNKNOWN;
  }

  public static String getMessageByCode(int code) {
    ErrorCodeEnum errorCodeEnum = getByCode(code);
    return errorCodeEnum.getMessage();
  }

  // 可以添加其他字段，如错误码分类、解决方案、建议等
  // private final ErrorCodeCategory category;
  // private final String solution;
  // private final String suggestion;

}
