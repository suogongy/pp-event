package org.ppj.pp.event.core.threadcontext;

public class ThreadContextSynchronizationManager {

    public static final String PEAK_CONTEXT_VALUE = "stress";
    public static final String NON_PEAK_CONTEXT_VALUE = "";

    private static volatile ThreadContextSynchronization threadContextSynchronization = new PeakThreadContextSynchronization();

    private String threadContext = null;

    private String currentThreadContext = null;


    public ThreadContextSynchronizationManager(String threadContext) {
        this.threadContext = threadContext;
    }

    public ThreadContextSynchronizationManager(Boolean isPeakContext) {
        this.threadContext = getContextValue(isPeakContext);
    }

    public static ThreadContextSynchronization getThreadContextSynchronization() {
        return threadContextSynchronization;
    }

    public static String getContextValue(boolean isPeakContext) {
        return isPeakContext ? PEAK_CONTEXT_VALUE : NON_PEAK_CONTEXT_VALUE;
    }

    public void executeWithBindThreadContext(Runnable runnable) {
        bindThreadContext();
        try {
            runnable.run();
        } finally {
            unbindThreadContext();
        }
    }

    public void bindThreadContext() {
        currentThreadContext = threadContextSynchronization.getCurrentThreadContext();

        threadContextSynchronization.setThreadContext(threadContext);
    }

    public void unbindThreadContext() {
        threadContextSynchronization.clear();

        threadContextSynchronization.setThreadContext(currentThreadContext);
    }
}
