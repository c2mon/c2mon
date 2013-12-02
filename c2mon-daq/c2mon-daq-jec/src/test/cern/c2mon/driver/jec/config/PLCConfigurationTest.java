package cern.c2mon.driver.jec.config;

import org.junit.Test;

import cern.c2mon.driver.jec.config.PLCConfiguration;
import static junit.framework.Assert.*;

public class PLCConfigurationTest {
    
    private PLCConfiguration plcConfiguration = new PLCConfiguration();
    
    private String addressString = "plc_name=plcstaa05,plcstaa06;" +
    		"Protocol=SiemensISO;Time_sync=Jec;Port=102;S_tsap=TCP-1;" +
    		"D_tsap=TCP-1;Alive_handler_period=5000;" +
    		"Dp_slave_address=4,5,6,7,8,9;";
    
    @Test
    public void testConfig() throws Exception {
        assertEquals(PLCConfiguration.DEFAULT_HANDLER_PERIOD, plcConfiguration.getHandlerPeriod());
        plcConfiguration.parsePLCAddress(addressString);
        assertEquals("plcstaa05", plcConfiguration.getPlcName());
        assertEquals("plcstaa06", plcConfiguration.getPlcNameRed());
        assertEquals("SiemensISO", plcConfiguration.getProtocol());
        assertEquals("Jec", plcConfiguration.getTimeSync());
        assertEquals(102, plcConfiguration.getPort());
        assertEquals("TCP-1", plcConfiguration.getsTsap());
        assertEquals("TCP-1", plcConfiguration.getdTsap());
        assertEquals(5000, plcConfiguration.getHandlerPeriod());
        assertEquals(6, plcConfiguration.getDpSlaveAddresses().size());
        boolean containsAllDpAddresses = false;
        for (int i = 4; i < 10; i++) {
            int dpSlaveAddress = plcConfiguration.getDpSlaveAddresses().get(i - 4);
            containsAllDpAddresses = dpSlaveAddress == i;
            if (!containsAllDpAddresses)
                break;
        }
        assertTrue(containsAllDpAddresses);
    }

}
