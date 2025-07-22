package org.ppj.pp.event.sample.mapper;

import org.ppj.pp.event.core.entity.PPEvent;
import org.ppj.pp.event.core.mapper.PPEventMapper;
import org.ppj.pp.event.sample.AbstractTestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
public class PPEventMapperTest extends AbstractTestCase {

    @Resource
    private PPEventMapper ppEventMapper;

    @Test
    @Ignore
    public void test1() {

        PPEvent ppEvent = ppEventMapper.findById(1l);

        ppEvent.getMethodInvocation().proceed();

        PPEvent ppEvent2 = ppEventMapper.findById(2l);

        ppEvent2.getMethodInvocation().proceed();
    }


    @Test
    public void testCount() {

        int count = ppEventMapper.count();
    }

    @Test
    @Ignore
    public void testBatchUpdate(){

        List<PPEvent> failedEvents = ppEventMapper.findFailedEventsByPreIdWithPaging(0, 10);

        if (CollectionUtils.isEmpty(failedEvents)){
            return;
        }

        failedEvents.forEach(PPEvent::reset);

        ppEventMapper.batchUpdate(failedEvents);
    }
}
