package com.anjia.unidbgserver.web;

import com.anjia.unidbgserver.response.enums.BussinsesEnum;
import com.anjia.unidbgserver.response.enums.ErrorCodeEnum;
import com.anjia.unidbgserver.response.Result;
import com.anjia.unidbgserver.service.PujiSigServiceWorker;
import jdk.nashorn.internal.ir.IfNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 控制类
 *
 * @author AnJia
 * @since 2021-07-26 18:31
 */
@Slf4j
@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class PujiSigController {

    @Resource(name = "pujisigWorker")
    private PujiSigServiceWorker pujisigServiceWorker;

    /**
     * 用于测试服务器状态
     *
     * @return pong  有效
     */
    @GetMapping("/get")
    public String get(){
        return "pong";
    }


    /**
     *  获取 pujisig
     * @return 结果
     */
    @PostMapping("/pujiSig")
    @SneakyThrows
    public Result getSig(@RequestBody  Map<String,String> map) {
        System.out.println(map);
        String str = map.get("str");
        if (StringUtils.isEmpty(str)){
            return Result.fail(ErrorCodeEnum.BAD_REQUEST.getCode(),ErrorCodeEnum.BAD_REQUEST.getMessage());
        }
        String opType = map.get("opType");
        BussinsesEnum bussinsesEnum = BussinsesEnum.valueOf(opType);
        if (ObjectUtils.isEmpty(bussinsesEnum)){
           return  Result.fail(ErrorCodeEnum.UNKNOWN_OPERATION_TYPE);
        }
        String key = map.get("key");
        if (!"123456".equals(key)){
           return  Result.fail(ErrorCodeEnum.UNAUTHORIZED);
        }
        String  sig = pujisigServiceWorker.getSign(str, bussinsesEnum).get();
        log.info("======================");
        log.info(sig);
        return Result.ok(sig);


    }




}
