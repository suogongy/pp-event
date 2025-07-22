package org.ppj.pp.event.core.processor;

import org.ppj.pp.event.core.configure.PPEventProperties;
import org.ppj.pp.event.core.entity.PPEvent;
import org.ppj.pp.event.core.eventhandle.PPEventHandler;
import org.ppj.pp.event.core.mapper.PPEventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

@Slf4j
public class EventMethodProcessor {
    @Autowired
    private PPEventProperties PPEventProperties;

    @Autowired
    private PPEventHandler ppEventHandler;

    @Resource
    private PPEventMapper ppEventMapper;

    public void handle(PPEvent ppEvent) {

        if (PPEventProperties.getRetryThreshold() < ppEvent.getRetriedCount()) {
            log.error("failed after max retry. eventInfo: {}", ppEvent.getMethodInvocationContent());
            ppEvent.markAsFailed();
            ppEventMapper.update(ppEvent);
            return;
        }

        ppEventHandler.handleEvent(ppEvent);
    }
}
