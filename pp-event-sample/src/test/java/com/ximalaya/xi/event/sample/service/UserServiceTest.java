package org.ppj.pp.event.sample.service;

import org.ppj.architecture.arena.common.constants.ArenaConstants;
import org.ppj.pp.event.core.threadcontext.ThreadContextSynchronizationManager;
import org.ppj.pp.event.sample.AbstractTestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class UserServiceTest extends AbstractTestCase {

    @Autowired
    private UserService userService;

    @Test
//    @Ignore
    public void test1() {

//        ThreadContextSynchronizationManager threadContextSynchronizationManager = new ThreadContextSynchronizationManager(ArenaConstants.PEAK_CONTEXT_VALUE);
//
//        threadContextSynchronizationManager.executeWithBindThreadContext(() -> {
//            int save = userService.save(1000001, "1号", 24, 1);
//
//            log.info("save result: {}", save);
//        });

        int save = userService.save(1000001, "1号", 24, 1);

        log.info("save result: {}", save);
    }
}
