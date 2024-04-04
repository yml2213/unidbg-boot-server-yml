package com.anjia.unidbgserver.req;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class FormRequest {

    @NotBlank(message = "str参数不能为空")
    private  String str;
    @NotBlank(message = "操作码参数不能为空")
    private  String opType;
    @NotBlank(message = "key参数不能为空")
    private String key;


}
