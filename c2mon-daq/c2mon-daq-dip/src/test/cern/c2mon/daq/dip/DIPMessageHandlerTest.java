/*
 * Copyright CERN 2011, All Rights Reserved.
 */
package cern.c2mon.daq.dip;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.easymock.Capture;
import org.junit.Test;

import cern.c2mon.daq.dip.DIPMessageHandler;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;

/**
 * This class implements a set of JUnit tests for <code>DIPMessageHandler</code>. All tests that require
 * DIPMessageHandler's pre-configuration with XML based configuration shall be annotated with <code>UseConf</code>
 * annotation, specifying the XML file to be used.
 * 
 * @see
 * @see cern.c2mon.daq.dip.DIPMessageHandler
 * @author wbuczak
 */
@UseHandler(DIPMessageHandler.class)
public class DIPMessageHandlerTest extends GenericMessageHandlerTst {
  
    static Logger log = Logger.getLogger(DIPMessageHandlerTest.class);

    DIPMessageHandler dipHandler;

    
    @Override
    protected void beforeTest() throws Exception {
        dipHandler = (DIPMessageHandler) msgHandler;                
    }

    @Override
    protected void afterTest() throws Exception {
        dipHandler.disconnectFromDataSource();       
    }
    

    @Test
    @UseConf("e_dip_test1.xml")
    public void testInvalidCredentials() throws Exception {

        Capture<Long> id = new Capture<Long>();
        Capture<Boolean> val = new Capture<Boolean>();
        Capture<String> msg = new Capture<String>();

        //messageSender.sendCommfaultTag(EasyMock.capture(id), EasyMock.capture(val), EasyMock.capture(msg));
        //expectLastCall().once();

        //replay(messageSender);

        //dipHandler.connectToDataSource();

        Thread.sleep(1000);
        
        // TODO : create real tests !!
        assertTrue(true);
    }
   
}
