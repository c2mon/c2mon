/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon.sender;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.almon.address.AlarmTriplet;
import cern.c2mon.daq.almon.address.UserProperties;
import cern.c2mon.daq.almon.sender.impl.AlarmMonitorSenderProxy;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;

/**
 * @author wbuczak
 */
public class AlarmMonitorSenderProxyTest {

    AlarmMonitorSenderProxy senderProxy;

    AlmonSender mockSender;

    private ScheduledExecutorService executor;

    private static final Logger LOG = LoggerFactory.getLogger(AlarmMonitorSenderProxyTest.class);

    private static final int CONCURRENT_TASKS_NUMBER = 16;

    private IEquipmentMessageSender ems;
    private ISourceDataTag sdt;

    @Before
    public void setUp() {
        senderProxy = new AlarmMonitorSenderProxy();
        List<AlmonSender> senders = new ArrayList<AlmonSender>();
        mockSender = mock(AlmonSender.class);
        senders.add(mockSender);
        senderProxy.setAlarmSenders(senders);

        executor = Executors.newScheduledThreadPool(8);

        ems = mock(IEquipmentMessageSender.class);
        sdt = mock(ISourceDataTag.class);
    }

    @After
    public void cleanUp() {
        executor.shutdown();
    }

    @Test
    public void testInstantiation() {
        assertNotNull(senderProxy);
    }

    @Test
    public void testConcurrentActivationsAndTerminations() throws Exception {

        final CountDownLatch latch = new CountDownLatch(CONCURRENT_TASKS_NUMBER * 2);

        final AlarmTriplet triplet = new AlarmTriplet("fault-family1", "fault-member1", 1);

        for (int i = 0; i < CONCURRENT_TASKS_NUMBER; i++) {
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    LOG.info("calling senderProxy.activate(triplet: {})", triplet.toString());
                    senderProxy.activate(sdt, ems, triplet, System.currentTimeMillis(), null);
                    latch.countDown();
                }
            }, 100, TimeUnit.MILLISECONDS);

        }// for

        for (int i = 0; i < CONCURRENT_TASKS_NUMBER; i++) {
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    LOG.info("calling senderProxy.terminate(triplet: {})", triplet.toString());
                    senderProxy.terminate(sdt, ems, triplet, System.currentTimeMillis());
                    latch.countDown();
                }
            }, 200, TimeUnit.MILLISECONDS);

        }// for

        latch.await(4000, TimeUnit.MILLISECONDS);

        // sender should be called once only,
        verify(mockSender, times(1)).activate(eq(sdt), eq(ems), eq(triplet), anyInt(), (UserProperties) anyObject());

        // sender should be called once only,
        verify(mockSender, times(1)).terminate(eq(sdt), eq(ems), eq(triplet), anyInt());
    }

    @Test
    public void testConcurrentUpdates() throws Exception {

        final CountDownLatch latch = new CountDownLatch(CONCURRENT_TASKS_NUMBER);

        final AlarmTriplet triplet = new AlarmTriplet("fault-family1", "fault-member1", 1);

        final UserProperties props = new UserProperties();
        props.put("userProperty1", "value1");
        props.put("userProperty2", "value2");

        senderProxy.activate(sdt, ems, triplet, System.currentTimeMillis(), null);

        for (int i = 0; i < CONCURRENT_TASKS_NUMBER; i++) {
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    LOG.info("calling senderProxy.activate(triplet: {})", triplet.toString());
                    senderProxy.update(sdt, ems, triplet, System.currentTimeMillis(), props);
                    latch.countDown();
                }
            }, 100, TimeUnit.MILLISECONDS);

        }// for

        latch.await(4000, TimeUnit.MILLISECONDS);

        // sender should be called once only,
        verify(mockSender, times(1)).update(eq(sdt), eq(ems), eq(triplet), anyInt(), (UserProperties) anyObject());
    }
}