package cern.c2mon.driver.common;

import java.lang.Thread.UncaughtExceptionHandler;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.driver.common.AliveTimer;
import cern.c2mon.driver.common.messaging.IProcessMessageSender;
import static org.easymock.classextension.EasyMock.*;

public class AliveTimerTest {
    private static final long INTERVAL = 10;
    private static final long TEST_TIME = 100;
    private static final int MAX_RUNS = Long.valueOf(1 + (TEST_TIME / INTERVAL)).intValue();
    private AliveTimer timer;
    private IProcessMessageSender sender;
    private Throwable uncaughtThrowable;

    @Before
    public void setUp() throws SecurityException, NoSuchMethodException {
        sender = createMock(IProcessMessageSender.class);
        timer = new AliveTimer(sender);
        final Thread jUnitThread = Thread.currentThread();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                if (!thread.equals(jUnitThread)) {
                    uncaughtThrowable = throwable;
                }
            }
        });
    }
    
    @Test
    public void testAliveTimer() throws Throwable {
        sender.sendAlive();
        /*
         *  Execution of alive timer is relative to previous execution.
         *  The range allows some error.
         */
        expectLastCall().times(MAX_RUNS/2, MAX_RUNS);
        replay(sender);
        timer.setInterval(INTERVAL);
        Thread.sleep(TEST_TIME);
        timer.terminateTimer();
        verify(sender);
        if (uncaughtThrowable != null) {
            throw uncaughtThrowable;
        }
    }

}
