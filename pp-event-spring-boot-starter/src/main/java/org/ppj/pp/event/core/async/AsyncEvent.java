package org.ppj.pp.event.core.async;

import org.ppj.pp.event.core.eventhandle.MethodInvocation;


public class AsyncEvent {

    public static final AsyncEventFactory FACTORY = new AsyncEventFactory();

    private MethodInvocation methodInvocation;
    private Long PPEventId;
    private String threadContext;

    public void reset(MethodInvocation methodInvocation,Long PPEventId,String threadContext) {
        this.methodInvocation = methodInvocation;
        this.PPEventId = PPEventId;
        this.threadContext = threadContext;
    }

    public MethodInvocation getMethodInvocation() {
        return methodInvocation;
    }

    public Long getPPEventId() {
        return PPEventId;
    }
    public String getThreadContext() {
        return threadContext;
    }

    public void clear() {
        methodInvocation = null;
        PPEventId = null;
        threadContext = null;
    }
}
