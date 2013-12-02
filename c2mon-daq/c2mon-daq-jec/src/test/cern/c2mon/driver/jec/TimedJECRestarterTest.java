package cern.c2mon.driver.jec;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.*;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.driver.jec.TimedJECRestarter;

public class TimedJECRestarterTest {
    
    private TimedJECRestarter timedJECRestarter;
    private EquipmentMessageHandler messageHandler = createMock(EquipmentMessageHandler.class);
    
    @Before
    public void setUp() {
        timedJECRestarter = new TimedJECRestarter(messageHandler, 100L, 100L);
    }
    
    @Test
    public void testForceImmediateRestart() throws EqIOException, InterruptedException {
        messageHandler.disconnectFromDataSource();
        messageHandler.connectToDataSource();
        
        replay(messageHandler);
        timedJECRestarter.forceImmediateRestart();
        Thread.sleep(200L);
        verify(messageHandler);
    }

    @Test
    public void testTimedRestart() throws EqIOException, InterruptedException {
        messageHandler.disconnectFromDataSource();
        messageHandler.connectToDataSource();
        
        replay(messageHandler);
        timedJECRestarter.triggerRestart();
        Thread.sleep(300L);
        verify(messageHandler);
    }
    
    @Test
    public void testMultipleTimedRestart() throws EqIOException, InterruptedException {
        messageHandler.disconnectFromDataSource();
        messageHandler.connectToDataSource();
        
        replay(messageHandler);
        // despite of the multiple restart triggers the restart should only occurre once.
        for (int i = 0; i < 100; i++) {
            timedJECRestarter.triggerRestart();
            Thread.sleep(10); // simulate work
        }
        Thread.sleep(300L);
        verify(messageHandler);
    }
}
