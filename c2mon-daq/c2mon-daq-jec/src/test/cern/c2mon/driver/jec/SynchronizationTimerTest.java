package cern.c2mon.driver.jec;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;

import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.driver.jec.PLCObjectFactory;
import cern.c2mon.driver.jec.SynchronizationTimer;
import cern.c2mon.driver.jec.config.PLCConfiguration;
import cern.c2mon.driver.jec.plc.StdConstants;
import cern.c2mon.driver.jec.plc.TestPLCDriver;

public class SynchronizationTimerTest {

    private SynchronizationTimer synchronizationTimer;
    private PLCObjectFactory plcFactory;
    
    @Before
    public void setUp() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        EquipmentLogger equipmentLogger =  new EquipmentLogger("asd", "asd", "asd");
        PLCConfiguration plcConfiguration = new PLCConfiguration();
        plcConfiguration.setProtocol("TestPLCDriver");
        plcFactory = new PLCObjectFactory(plcConfiguration);
        synchronizationTimer = new SynchronizationTimer(equipmentLogger, plcFactory);
    }
    
    @Test
    public void testAdjustment() {
        if (new GregorianCalendar().getTimeZone().inDaylightTime(new Date())) {
            GregorianCalendar calendar = new GregorianCalendar(2010, 12, 1);
            Date winterDate = calendar.getTime();
            synchronizationTimer.testDaylightSavingTime(winterDate);
        }
        else {
            GregorianCalendar calendar = new GregorianCalendar(2010, 6, 1);
            Date summerDate = calendar.getTime();
            synchronizationTimer.testDaylightSavingTime(summerDate);
        }
        TestPLCDriver driver = (TestPLCDriver) plcFactory.getPLCDriver();
        assertEquals(driver.getLastSend().getMsgID(), StdConstants.SET_TIME_MSG);
    }
    
    @Test
    public void testNoAdjustment() {
        if (new GregorianCalendar().getTimeZone().inDaylightTime(new Date())) {
            GregorianCalendar calendar = new GregorianCalendar(2010, 6, 1);
            Date summerDate = calendar.getTime();
            synchronizationTimer.testDaylightSavingTime(summerDate);
        }
        else {
            GregorianCalendar calendar = new GregorianCalendar(2010, 12, 1);
            Date winterDate = calendar.getTime();
            synchronizationTimer.testDaylightSavingTime(winterDate);
        }
        TestPLCDriver driver = (TestPLCDriver) plcFactory.getPLCDriver();
        assertNull(driver.getLastSend());
    }
}
