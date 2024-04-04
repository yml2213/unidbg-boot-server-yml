package com.anjia.unidbgserver.response.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 错误码枚举类
 *
 * @author cym
 */
@Getter
public enum BussinsesEnum {
    /**
     * 业务操作码
     */

    GET_SIG(10000,"获取sig操作"),
    GET_SIG3(10001,"获取sig3操作"),







    ;



  private final int code;
  private final String message;

  BussinsesEnum( int code, String message) {
    this.code = code;
    this.message = message;
  }





  // 可以添加其他字段，如错误码分类、解决方案、建议等
  // private final ErrorCodeCategory category;
  // private final String solution;
  // private final String suggestion;

}
