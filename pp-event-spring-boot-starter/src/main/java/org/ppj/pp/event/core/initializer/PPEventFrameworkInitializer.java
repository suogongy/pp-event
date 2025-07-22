package org.ppj.pp.event.core.initializer;

import org.ppj.pp.event.core.register.RegisterService;
import lombok.extern.java.Log;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import javax.annotation.Resource;

@Log
public class PPEventFrameworkInitializer implements ApplicationRunner {

    @Resource
    private RegisterService registerService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        log.info("try init pp event framework!");

        registerService.initJob();

        log.info("pp event framework init finished!");
    }
}
