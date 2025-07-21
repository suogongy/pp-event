package org.ppj.pp.event.sample.job;

import org.ppj.pp.event.core.job.EventHandleRecoverJob;
import org.ppj.pp.event.sample.AbstractTestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

public class RecoverJobTest extends AbstractTestCase {

    @Autowired
    private EventHandleRecoverJob eventHandleRecoverJob;

    @Test
    @Ignore
    public void testRecover(){
        eventHandleRecoverJob.eventHandleRecoverJobHandler();

        try {
            TimeUnit.SECONDS.sleep(10l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
