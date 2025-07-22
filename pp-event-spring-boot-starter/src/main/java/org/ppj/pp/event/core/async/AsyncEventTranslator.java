package org.ppj.pp.event.core.async;

import com.lmax.disruptor.EventTranslator;
import org.ppj.pp.event.core.eventhandle.MethodInvocation;

public class AsyncEventTranslator implements EventTranslator<AsyncEvent>, AutoCloseable {

    private MethodInvocation methodInvocation;
    private Long PPEventId;
    private String threadContext;

    public AsyncEventTranslator(MethodInvocation methodInvocation,Long PPEventId,String threadContext) {
        this.methodInvocation = methodInvocation;
        this.PPEventId = PPEventId;
        this.threadContext = threadContext;
    }

    @Override
    public void translateTo(AsyncEvent event, long sequence) {
        event.reset(methodInvocation,PPEventId,threadContext);
    }

    public MethodInvocation getMethodInvocation() {
        return methodInvocation;
    }

    public Long getPPEventId() {
        return PPEventId;
    }

    @Override
    public void close() throws Exception {
        this.methodInvocation = null;
        this.PPEventId = null;
        this.threadContext = null;
    }
}
