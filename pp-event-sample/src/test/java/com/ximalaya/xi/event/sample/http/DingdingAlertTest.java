package org.ppj.pp.event.sample.http;

import org.ppj.pp.event.core.job.FailedEventWarnJob;
import org.ppj.pp.event.sample.AbstractTestCase;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Resource;

public class DingdingAlertTest extends AbstractTestCase {

    @Resource
    private FailedEventWarnJob failedEventWarnJob;

    @Test
    @Ignore
    public void test(){
//        failedEventWarnJob.sendDingDingAlert();
    }
}
