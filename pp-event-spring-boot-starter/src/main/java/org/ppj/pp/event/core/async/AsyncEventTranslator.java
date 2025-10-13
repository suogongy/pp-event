package org.ppj.pp.event.core.async;

import com.lmax.disruptor.EventTranslator;
import org.ppj.pp.event.core.eventhandle.MethodInvocation;

public class AsyncEventTranslator implements EventTranslator<AsyncEvent>, AutoCloseable {

    private MethodInvocation methodInvocation;
    private Long ppEventId;

    public AsyncEventTranslator(MethodInvocation methodInvocation,Long PPEventId) {
        this.methodInvocation = methodInvocation;
        this.ppEventId = PPEventId;
    }

    @Override
    public void translateTo(AsyncEvent event, long sequence) {
        event.reset(methodInvocation, ppEventId);
    }

    public MethodInvocation getMethodInvocation() {
        return methodInvocation;
    }

    public Long getPpEventId() {
        return ppEventId;
    }

    @Override
    public void close() throws Exception {
        this.methodInvocation = null;
        this.ppEventId = null;
    }
}
