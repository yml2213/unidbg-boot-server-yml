package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
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
    private TTEncryptService ttEncryptService;

    private PujiSigService pujiSigService;

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
                                @Value("${spring.task.execution.pool.core-size:4}") int poolSize) {
        super(WorkerPoolFactory.create(PujiSigServiceWorker::new, Runtime.getRuntime().availableProcessors()));
        this.unidbgProperties = unidbgProperties;
        if (this.unidbgProperties.isAsync()) {
            pool = WorkerPoolFactory.create(pool -> new PujiSigServiceWorker(unidbgProperties.isDynarmic(),
                unidbgProperties.isVerbose(), pool), Math.max(poolSize, 4));
            log.info("线程池为:{}", Math.max(poolSize, 4));
        } else {
            this.pujiSigService = new PujiSigService(unidbgProperties);

//            this.ttEncryptService = new TTEncryptService(unidbgProperties);
        }
    }

    public PujiSigServiceWorker(boolean dynarmic, boolean verbose, WorkerPool pool) {
        super(pool);
        this.unidbgProperties = new UnidbgProperties();
        unidbgProperties.setDynarmic(dynarmic);
        unidbgProperties.setVerbose(verbose);
        log.info("是否启用动态引擎:{},是否打印详细信息:{}", dynarmic, verbose);
        this.pujiSigService = new PujiSigService(unidbgProperties);
//        this.ttEncryptService = new TTEncryptService(unidbgProperties);
    }

    @Async
    @SneakyThrows
    public CompletableFuture<String>  getSig(String str) {

        PujiSigServiceWorker worker;
        String sig;
        if (this.unidbgProperties.isAsync()) {
            while (true) {
                if ((worker = pool.borrow(2, TimeUnit.SECONDS)) == null) {
                    continue;
                }
                sig = worker.doWork(str);
                pool.release(worker);
                break;
            }
        } else {
            synchronized (this) {
                sig = this.doWork(str);
            }
        }
        return CompletableFuture.completedFuture(sig);
    }

    private String doWork(String str) {
        return pujiSigService.getClock(str);
//        return ttEncryptService.ttEncrypt(body);
    }

    @SneakyThrows
    @Override public void destroy() {
        pujiSigService.destroy();
    }
}
