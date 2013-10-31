/*
 * Copyright CERN 2011, All Rights Reserved.
 */
package cern.c2mon.daq.laser;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;

import cern.c2mon.daq.laser.LASERMessageHandler;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;

/**
 * This class implements a set of JUnit tests for <code>LASERMessageHandler</code>. All tests that require
 * LASERMessageHandler pre-configuration with XML based configuration shall be annotated with <code>UseConf</code>
 * annotation, specifying the XML file to be used.
 * 
 * @see
 * @see cern.c2mon.daq.laser.LASERMessageHandler 
 * @author wbuczak
 */
@UseHandler(LASERMessageHandler.class)
public class LASERMessageHandlerTest extends GenericMessageHandlerTst {
  
    static Logger log = Logger.getLogger(LASERMessageHandlerTest.class);

    LASERMessageHandler laserHandler;

    
    @Override
    protected void beforeTest() throws Exception {
        laserHandler = (LASERMessageHandler) msgHandler;                
    }

    @Override
    protected void afterTest() throws Exception {
        laserHandler.disconnectFromDataSource();       
    }
    

    @Test
    @UseConf("e_laser_test1.xml")
    public void test1() throws Exception {
      
        // TODO : create real tests !!
        assertTrue(true);
   }
   
}
