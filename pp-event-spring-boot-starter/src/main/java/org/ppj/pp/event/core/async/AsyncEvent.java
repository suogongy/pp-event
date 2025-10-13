package org.ppj.pp.event.core.async;

import org.ppj.pp.event.core.eventhandle.MethodInvocation;


public class AsyncEvent {

    public static final AsyncEventFactory FACTORY = new AsyncEventFactory();

    private MethodInvocation methodInvocation;
    private Long ppEventId;

    public void reset(MethodInvocation methodInvocation,Long PPEventId) {
        this.methodInvocation = methodInvocation;
        this.ppEventId = PPEventId;
    }

    public MethodInvocation getMethodInvocation() {
        return methodInvocation;
    }

    public Long getPpEventId() {
        return ppEventId;
    }

    public void clear() {
        methodInvocation = null;
        ppEventId = null;
    }
}
