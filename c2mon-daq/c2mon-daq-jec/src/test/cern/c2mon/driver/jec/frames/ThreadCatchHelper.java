package cern.c2mon.driver.jec.frames;

import junit.framework.AssertionFailedError;

public class ThreadCatchHelper {

    private Object lock = new Object();
    private String problemMessage;
    private StackTraceElement[] problemStackTrace;

    public ThreadCatchHelper() {
        super();
    }

    /**
     * Catch uncatched exceptions in other threads.
     * 
     * @param junitMainThread
     */
    protected void prepareForAsynchronousFailureHandling(final Thread junitMainThread) {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (!t.equals(junitMainThread)) {
                    synchronized (lock) {
                        problemMessage = e.getMessage();
                        problemStackTrace = e.getStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Check if there was an exception and throw it in the main thread.
     */
    protected void reportPossibleProblemsInListenerWithMethod() {
        synchronized (lock) {
            if (problemMessage != null) {
                final AssertionFailedError ae = new AssertionFailedError(problemMessage);
                ae.setStackTrace(problemStackTrace); 
                throw ae;
            }
        }
    }

}