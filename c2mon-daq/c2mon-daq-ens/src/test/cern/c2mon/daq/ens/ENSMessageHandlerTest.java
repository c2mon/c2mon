/*
 * Copyright CERN 2011, All Rights Reserved.
 */
package cern.c2mon.daq.ens;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;

import cern.c2mon.daq.ens.ENSMessageHandler;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;

/**
 * This class implements a set of JUnit tests for <code>ENSMessageHandler</code>. All tests that require
 * ENSMessageHandler pre-configuration with XML based configuration shall be annotated with <code>UseConf</code>
 * annotation, specifying the XML file to be used.
 * 
 * @see
 * @see cern.c2mon.daq.ens.ENSMessageHandler
 * @author wbuczak
 */
@UseHandler(ENSMessageHandler.class)
public class ENSMessageHandlerTest extends GenericMessageHandlerTst {
  
    static Logger log = Logger.getLogger(ENSMessageHandlerTest.class);

    ENSMessageHandler ensHandler;

    
    @Override
    protected void beforeTest() throws Exception {
        ensHandler = (ENSMessageHandler) msgHandler;                
    }

    @Override
    protected void afterTest() throws Exception {
        ensHandler.disconnectFromDataSource();       
    }
    

    @Test
    @UseConf("e_ens_test1.xml")
    public void test1() throws Exception {
      
        // TODO : create real tests !!
        assertTrue(true);
   }
   
}
