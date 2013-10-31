package cern.c2mon.daq.common;

//import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Timer;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.EquipmentLogger;
import cern.c2mon.daq.common.EquipmentMessageSender;
import cern.c2mon.daq.common.SDTTimeDeadbandScheduler;
import cern.c2mon.daq.common.conf.core.EquipmentConfiguration;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.daq.filter.dynamic.IDynamicTimeDeadbandFilterActivator;
import cern.c2mon.daq.tools.DataTagValueChecker;
import cern.tim.shared.common.datatag.DataTagAddress;
import cern.tim.shared.daq.datatag.SourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataTagValue;
import cern.tim.shared.daq.filter.FilteredDataTagValue;

public class SDTTimeDeadbandSchedulerTest {
    
    private IProcessMessageSender processMessageSender;
    private SDTTimeDeadbandScheduler scheduler;
    private SourceDataTag tag;
    private IFilterMessageSender filterMessageSender;
    private EquipmentMessageSender equipmentMessageSender;
    private Throwable exception = null;

    @Before
    public void setUp() {
        processMessageSender = createMock(IProcessMessageSender.class);
        filterMessageSender = createMock(IFilterMessageSender.class);
        IDynamicTimeDeadbandFilterActivator activator = createMock(IDynamicTimeDeadbandFilterActivator.class);
        equipmentMessageSender = new EquipmentMessageSender(
                filterMessageSender, processMessageSender, activator , activator);
        DataTagAddress address = new DataTagAddress();
        address.setTimeDeadband(30);
        tag = new SourceDataTag(1L, "asd", false);
        tag.setAddress(address);
        EquipmentConfiguration conf = new EquipmentConfiguration();
        conf.getDataTags().put(1L, tag);
        equipmentMessageSender.setEquipmentConfiguration(conf);
        tag.update("");
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                exception = e;
            }
        });
    }

    @Test
    public void testSchedule() throws Exception {
        processMessageSender.addValue(isA(SourceDataTagValue.class));
        replay(processMessageSender);
        EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
        scheduler = new SDTTimeDeadbandScheduler(tag, equipmentMessageSender, 
                new Timer(true), new DataTagValueChecker(equipmentLogger));
        scheduler.scheduleValueForSending();
        Thread.sleep(50);
        verify(processMessageSender);
        if (exception != null)
            throw new Exception(exception);
    }
    
    @Test
    public void testScheduleRepeated() throws Exception {
        processMessageSender.addValue(isA(SourceDataTagValue.class));
        filterMessageSender.addValue(isA(FilteredDataTagValue.class));
        replay(processMessageSender, filterMessageSender);
        EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
        scheduler = new SDTTimeDeadbandScheduler(tag, equipmentMessageSender, 
                new Timer(true), new DataTagValueChecker(equipmentLogger));
        scheduler.scheduleValueForSending();
        Thread.sleep(50);
        // repeated value
        scheduler.scheduleValueForSending();
        Thread.sleep(100);
        verify(processMessageSender, filterMessageSender);
        if (exception != null)
            throw new Exception(exception);
    }
}
