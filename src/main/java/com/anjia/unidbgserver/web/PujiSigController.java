package com.anjia.unidbgserver.web;

import com.anjia.unidbgserver.response.enums.BussinsesEnum;
import com.anjia.unidbgserver.response.enums.ErrorCodeEnum;
import com.anjia.unidbgserver.response.Result;
import com.anjia.unidbgserver.service.PujiSigServiceWorker;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
    @PostMapping("/puji-sig")
    @SneakyThrows
    public Result<String> getSig(@RequestBody  Map<String,String> map) {
        System.out.println(map);
        String str = map.get("sig");
        if (StringUtils.isEmpty(str)){
            Result.fail(ErrorCodeEnum.BAD_REQUEST);
        }
        String  sig = pujisigServiceWorker.getSign(str, BussinsesEnum.GET_SIG).get();
        log.info("======================");
        log.info(sig);
        return Result.ok(sig);
    }

    @PostMapping("/puji-sig3")
    @SneakyThrows
    public Result<String> getSig3(@RequestBody  Map<String,String> map) {
        System.out.println(map);
        String str = map.get("path");
        if (StringUtils.isEmpty(str)){
            Result.fail(ErrorCodeEnum.BAD_REQUEST);
        }
        String  sig = pujisigServiceWorker.getSign(str, BussinsesEnum.GET_SIG3).get();
        log.info("======================");
        log.info(sig);
        return Result.ok(sig);
    }


}
