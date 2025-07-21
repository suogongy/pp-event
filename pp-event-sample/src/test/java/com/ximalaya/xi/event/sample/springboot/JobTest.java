package org.ppj.pp.event.sample.springboot;

import org.ppj.pp.event.core.job.EventHandleRecoverJob;
import org.ppj.pp.event.sample.AbstractTestCase;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Resource;

public class JobTest extends AbstractTestCase {

    @Resource
    private EventHandleRecoverJob eventHandleRecoverJob;

    @Test
    @Ignore
    public void testRecover(){

        try {
            eventHandleRecoverJob.eventHandleRecoverJobHandler();
        } catch (Exception exception) {

        }
    }
}
