package org.ppj.pp.event.core.threadcontext;

public class PeakThreadContextSynchronization implements ThreadContextSynchronization {

    @Override
    public String getCurrentThreadContext() {
        return XdcsTool.getPeakRequestVal();
    }

    @Override
    public void setThreadContext(String threadContext) {
        XdcsTool.setPeakRequestVal(threadContext);
    }

    @Override
    public void clear() {
        XdcsTool.clearPeakRequestVal();
    }
}
