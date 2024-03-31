package com.anjia.unidbgserver.response;

  import lombok.Data;
  import lombok.experimental.Accessors;

  /**
   * @Author: cym
   * @create: 2023-02-21 10:10
   * @description: 统一返回结果类
   * @version: 1.0
   **/
  @Data
  @Accessors(chain = true)
  public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    private String timestamp;



    private static <T> Result<T> build(T body, Integer code, String message) {
      Result<T> result = new Result<>();
      if (body != null) {
        result.setData(body);
      }
      result.setCode(code);
      result.setMessage(message);
      return result;
    }

    private static <T> Result<T> build(Integer code, String message) {
      Result<T> result = new Result<>();
      result.setCode(code);
      result.setMessage(message);
      return result;
    }



    /**
     * 操作成功
     *
     * @param data 数据
     * @return 返回统一结果集
     */
    public static <T> Result<T> ok(T data) {
      return build(data, 200, "成功");
    }

    /**
     * 操作成功
     *
     * @return 返回统一结果集
     */
    public static <T> Result<T> ok() {
      return build(null, 200, "成功");
    }

    /**
     * 操作失败
     *
     * @param data 数据
     * @return 统一返回结果集
     */
    public static <T> Result<T> fail(T data) {
      return build(data, 201, "操作失败");
    }


    /**
     * 操作失败
     *
     * @return 统一返回结果集
     */
    public static <T> Result<T> fail(Integer code,String message) {
      return build(code,message);
    }


    public static <T> Result<T> fail(T data, Integer code, String message) {
      return build(data, code, message);
    }


  }
