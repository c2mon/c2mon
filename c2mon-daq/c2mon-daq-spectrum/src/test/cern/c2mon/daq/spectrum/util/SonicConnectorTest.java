/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum.util;

import static org.junit.Assert.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.spectrum.SpectrumMessageHandler;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseHandler;

@UseHandler(SpectrumMessageHandler.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SonicConnectorTest extends GenericMessageHandlerTst {

    Logger LOG = LoggerFactory.getLogger(SonicConnectorTest.class);
        
    //
    // --- TEST --------------------------------------------------------------------------------
    //    
    /**
     * Check that the default connection returned by the SonicConnector class is not null, which is
     * the case when the connection does not succed. Any other exception during the test is 
     * considered to be an error.
     */
//    @Test
    public void testSonicConnector()
    {
        try
        {
            javax.jms.Connection conn = (new SonicConnector()).getConnection();
            assertNotNull(conn);
            Thread.sleep(3000);
            conn.close();
            Thread.sleep(3000);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
        
    //
    // --- SETUP --------------------------------------------------------------------------------
    //
    @Override
    protected void beforeTest() throws Exception {
        //
    }

    @Override
    protected void afterTest() throws Exception {
        //
    }
}


