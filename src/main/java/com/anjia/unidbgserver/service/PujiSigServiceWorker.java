package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import com.anjia.unidbgserver.response.enums.BussinsesEnum;
import com.anjia.unidbgserver.service.original.PujiClientSignService;
import com.anjia.unidbgserver.service.unidbg.PujiSig3Service;
import com.anjia.unidbgserver.service.unidbg.PujiSigService;
import com.github.unidbg.worker.Worker;
import com.github.unidbg.worker.WorkerPool;
import com.github.unidbg.worker.WorkerPoolFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("pujisigWorker")
public class PujiSigServiceWorker extends Worker {

    private UnidbgProperties unidbgProperties;
    private WorkerPool pool;

    private PujiSig3Service pujiSig3Service;

    private PujiSigService pujiSigService;

    private PujiClientSignService pujiClientSignService;

    @Autowired
    public void init(UnidbgProperties unidbgProperties) {
        this.unidbgProperties = unidbgProperties;
    }

    public PujiSigServiceWorker() {
        super(WorkerPoolFactory.create(PujiSigServiceWorker::new, Runtime.getRuntime().availableProcessors()));
    }

    public PujiSigServiceWorker(WorkerPool pool) {
        super(pool);
    }

    @Autowired
    public PujiSigServiceWorker(UnidbgProperties unidbgProperties,
                                @Value("${spring.task.execution.pool.core-size}") int poolSize) {
        super(WorkerPoolFactory.create(PujiSigServiceWorker::new, Runtime.getRuntime().availableProcessors()));
        this.unidbgProperties = unidbgProperties;
        log.info("是否启用动态引擎:{},是否打印详细信息:{}", unidbgProperties.isDynarmic(), unidbgProperties.isVerbose());

        if (this.unidbgProperties.isAsync()) {
            pool = WorkerPoolFactory.create(pool -> new PujiSigServiceWorker(unidbgProperties, pool), Math.max(poolSize, 4));
            log.info("线程池为:{}", Math.max(poolSize, 4));
        } else {
            this.pujiSigService = new PujiSigService(unidbgProperties);
            this.pujiSig3Service = new PujiSig3Service(unidbgProperties);
            this.pujiClientSignService = new PujiClientSignService();
        }
    }

    public PujiSigServiceWorker(UnidbgProperties unidbgProperties, WorkerPool pool) {
        super(pool);
        this.pujiSigService = new PujiSigService(unidbgProperties);
        this.pujiSig3Service = new PujiSig3Service(unidbgProperties);
        this.pujiClientSignService = new PujiClientSignService();
    }

    @Async
    @SneakyThrows
    public CompletableFuture<String> getSign(String str, BussinsesEnum bussinsesEnum) {

        PujiSigServiceWorker worker;
        String sig;
        if (this.unidbgProperties.isAsync()) {
            while (true) {
                if ((worker = pool.borrow(2, TimeUnit.SECONDS)) == null) {
                    continue;
                }
                sig = worker.doWork(str, bussinsesEnum);
                pool.release(worker);
                break;
            }
        } else {
            synchronized (this) {
                sig = this.doWork(str, bussinsesEnum);
            }
        }
        return CompletableFuture.completedFuture(sig);
    }


    private String doWork(String str, BussinsesEnum bussinsesEnum) {
        int code = bussinsesEnum.getCode();
        if (code == BussinsesEnum.GET_SIG.getCode()) {
            return pujiSigService.getClock(str);
        } else if (code == BussinsesEnum.GET_SIG3.getCode()) {
            return pujiSig3Service.getNsSig3(str);
        } else if (code == BussinsesEnum.GET_CLIENTSIGN.getCode()) {
            return pujiClientSignService.getClientSign(str);
        } else if (code == BussinsesEnum.GET_SIG3_64.getCode()) {
            return pujiSig3Service.getNsSig3(str, "010a11c6-f2cb-4016-887d-0d958aef1534");
        }
        return "";


    }

    @SneakyThrows
    @Override
    public void destroy() {
        pujiSigService.destroy();
        pujiSig3Service.destroy();
    }
}
