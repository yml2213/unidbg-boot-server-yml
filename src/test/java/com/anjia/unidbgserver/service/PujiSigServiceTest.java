package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import com.anjia.unidbgserver.service.unidbg.PujiSig3Service;
import com.anjia.unidbgserver.service.unidbg.PujiSigService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 单元测试
 *
 * @author AnJia
 * @since 2021-08-02 16:31
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PujiSigServiceTest {

    @Autowired
    PujiSigServiceWorker pujisigServiceWorker;

    @Autowired
    UnidbgProperties properties;


    @SneakyThrows
    @Test
    void testServiceGetTTEncrypt() {



        PujiSigService pujisigService = new PujiSigService(properties);
        String str = "";
        String data = pujisigService.getClock(str);
        log.info(data);
    }

    @SneakyThrows
    @Test
    void testSig3() {

        PujiSig3Service pujisig3Service = new PujiSig3Service(properties);

        pujisig3Service.initNative();
        String nsSig3 = pujisig3Service.getNsSig3("/dasddsaas");
        log.info(nsSig3);
    }




}
