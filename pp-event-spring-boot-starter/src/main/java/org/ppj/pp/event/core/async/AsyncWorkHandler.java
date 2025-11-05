package org.ppj.pp.event.core.async;

import com.lmax.disruptor.WorkHandler;
import org.ppj.pp.event.core.eventhandle.PPEventHandler;
import org.ppj.pp.event.core.factory.FactoryBuilder;
import org.ppj.pp.event.core.threadcontext.ThreadContextSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;

public class AsyncWorkHandler implements WorkHandler<AsyncEvent> {

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
        PPEventHandler ppEventHandler = FactoryBuilder.factoryOf(PPEventHandler.class).getInstance();
        ppEventHandler.handleEvent(event.getPpEventId());
    }
}
