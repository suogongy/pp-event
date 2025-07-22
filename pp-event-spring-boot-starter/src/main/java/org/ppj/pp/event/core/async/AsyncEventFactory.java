package org.ppj.pp.event.core.async;

import com.lmax.disruptor.EventFactory;

public class AsyncEventFactory implements EventFactory<AsyncEvent> {
    @Override
    public AsyncEvent newInstance() {
        return new AsyncEvent();
    }
}
