package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import com.anjia.unidbgserver.web.PujiSigController;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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

    @Autowired
    private PujiSigController pujiSigController;

    private MockMvc mockMvc;
    @Before
    public void setUp(){
        mockMvc = MockMvcBuilders.standaloneSetup(pujiSigController).build();
    }
    @SneakyThrows @Test
    void testServiceGetTTEncrypt() {


        PujiSigService pujisigService = new PujiSigService(properties);
        String str = "";
        String data = pujisigService.getClock(str);
        log.info(data);
    }

//    @SneakyThrows @Test
//    void testWorkerGetTTEncrypt() {
//        byte[] data = pujisigServiceWorker.getSig(null, null).get();
//        log.info(new String(data));
//    }
}
