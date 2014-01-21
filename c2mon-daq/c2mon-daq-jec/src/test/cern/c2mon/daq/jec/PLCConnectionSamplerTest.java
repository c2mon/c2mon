package cern.c2mon.daq.jec;

import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.jec.IJECRestarter;
import cern.c2mon.daq.jec.PLCConnectionSampler;

public class PLCConnectionSamplerTest {

    private static final long SAMPLER_PERIOD = 100;
    private PLCConnectionSampler plcConnectionSampler;
    private IJECRestarter jecRestarter = createMock(IJECRestarter.class);
    
    @Before
    public void setUp() {
        EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
        plcConnectionSampler = new PLCConnectionSampler(jecRestarter, equipmentLogger, SAMPLER_PERIOD);
    }
    
    @Test
    public void testConectionLost() throws InterruptedException {
        plcConnectionSampler.start();
        plcConnectionSampler.updateAliveTimer();
        jecRestarter.forceImmediateRestart();
        
        replay(jecRestarter);
        for (int i = 0; i < 10; i++) {
            Thread.sleep(50);
            plcConnectionSampler.updateAliveTimer();
        }
        // no call to jecRestarter should have happened.
        Thread.sleep(300); // sleep to provoke call.
        verify(jecRestarter);
    }
}
