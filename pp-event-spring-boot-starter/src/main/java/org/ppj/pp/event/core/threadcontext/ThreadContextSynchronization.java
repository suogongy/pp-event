package org.ppj.pp.event.core.threadcontext;

public interface ThreadContextSynchronization {

    String getCurrentThreadContext();

    void setThreadContext(String threadContext);

    void clear();
}
