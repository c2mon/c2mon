/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.common.messaging.impl;

import java.lang.Thread.UncaughtExceptionHandler;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.common.messaging.impl.AliveTimer; 
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
        sender.sendProcessAlive();
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
