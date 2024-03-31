package com.anjia.unidbgserver.web;

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
     * <p>
     * public byte[] ttEncrypt(@RequestParam(required = false) String key1, @RequestBody String body)
     * // 这是接收一个url参数，名为key1,接收一个post或者put请求的body参数
     * key1是选填参数，不写也不报错，值为,body只有在请求方法是POST时才有，GET没有
     *
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
        String  sig = pujisigServiceWorker.getSig(str).get();
        log.info("======================");
        log.info(sig);
        return Result.ok(sig);

    }


}
