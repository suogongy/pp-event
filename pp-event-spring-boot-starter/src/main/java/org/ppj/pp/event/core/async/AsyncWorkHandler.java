package org.ppj.pp.event.core.async;

import com.lmax.disruptor.WorkHandler;
import org.ppj.pp.event.core.eventhandle.PPEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class AsyncWorkHandler implements WorkHandler<AsyncEvent> {

    @Resource
    private PPEventHandler ppEventHandler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onEvent(AsyncEvent event) throws Exception {

        try {
            doWorkHandle(event);
        } finally {
            event.clear();
        }
    }

    private void doWorkHandle(AsyncEvent event) {
        ppEventHandler.handleEvent(event.getPpEventId());
    }
}
