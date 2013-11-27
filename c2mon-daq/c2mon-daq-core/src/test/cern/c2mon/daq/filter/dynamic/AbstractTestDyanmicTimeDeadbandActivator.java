package cern.c2mon.daq.filter.dynamic;

import static junit.framework.Assert.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cern.c2mon.daq.filter.dynamic.IDynamicTimeDeadbandFilterActivator;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.daq.datatag.SourceDataTag;

public abstract class AbstractTestDyanmicTimeDeadbandActivator {

    private static final int NUMBER_OF_TEST_TAGS = 1000;
    private static final Long TEST_KEY = 1L;
    private static SourceDataTag testTag = new SourceDataTag(getTestKey(), "NoName", true);
    private static Collection<SourceDataTag> tags = new ConcurrentLinkedQueue<SourceDataTag>();
    private static HashMap<Long, SourceDataTag> sourceDataTags = new HashMap<Long, SourceDataTag>();
    private static IDynamicTimeDeadbandFilterActivator activator;
    private volatile Throwable error;

    @Before
    public void setUpException() {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                error = e;
            }
        });
    }

    @After
    public void tearDown() throws Throwable {
        if (error != null)
            throw error;
    }

    @BeforeClass
    public static void setUpClass() {
        getSourceDataTags().put(getTestKey(), getTestTag());
        DataTagAddress address = new DataTagAddress();
        address.setTimeDeadband(0);
        getTestTag().setAddress(address);
    }

    @Test
    public void testDataTagAddRemove() {
        SourceDataTag tag = new SourceDataTag(2L, "123", false);
        DataTagAddress address = new DataTagAddress();
        address.setTimeDeadband(30);
        tag.setAddress(address);
        activator.addDataTag(tag);
        assertTrue(tag.getAddress().isTimeDeadbandEnabled());
        assertNotNull(activator.getDataTagMap().get(2L));
        activator.removeDataTag(tag);
        assertNull(activator.getDataTagMap().get(2L));
        assertFalse(tag.getAddress().isTimeDeadbandEnabled());
    }

    @Test
    public void testMultiThread() throws InterruptedException {
        Collection<Timer> timers = new ArrayList<Timer>();
        for (long i = 0; i < NUMBER_OF_TEST_TAGS; i++) {
            DataTagAddress address = new DataTagAddress();
            address.setTimeDeadband(0);
            activator.addDataTag(
                    new SourceDataTag(i, "asd", false, (short)0, "Boolean", address));
        }
        for (int j = 0; j < 10; j++) {
            Timer timer = new Timer();
            timers.add(timer);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    for (int i = 0; i < NUMBER_OF_TEST_TAGS; i++)
                        activator.newTagValueSent(
                                Double.valueOf(
                                        Math.random() * NUMBER_OF_TEST_TAGS - 1).intValue());
                }
            }, 0, 1);
        }
        Thread.sleep(5000L);
        for (Timer timer : timers) {
            timer.cancel();
        }
        activator.clearDataTags();
        Thread.sleep(1000L);
    }

    public static void setSourceDataTags(HashMap<Long, SourceDataTag> sourceDataTags) {
        AbstractTestDyanmicTimeDeadbandActivator.sourceDataTags = sourceDataTags;
    }

    public static HashMap<Long, SourceDataTag> getSourceDataTags() {
        return sourceDataTags;
    }

    public static void setActivator(IDynamicTimeDeadbandFilterActivator activator) {
        AbstractTestDyanmicTimeDeadbandActivator.activator = activator;
    }

    public static IDynamicTimeDeadbandFilterActivator getActivator() {
        return activator;
    }

    public static void setTestTag(SourceDataTag testTag) {
        AbstractTestDyanmicTimeDeadbandActivator.testTag = testTag;
    }

    public static SourceDataTag getTestTag() {
        return testTag;
    }

    public static Long getTestKey() {
        return TEST_KEY;
    }
}