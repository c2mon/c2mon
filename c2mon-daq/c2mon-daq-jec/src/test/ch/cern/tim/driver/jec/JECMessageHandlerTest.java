/*
 * Copyright CERN 2011, All Rights Reserved.
 */
package ch.cern.tim.driver.jec;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.management.ManagementFactory;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;

import cern.tim.driver.test.GenericMessageHandlerTst;
import cern.tim.driver.test.SourceDataTagValueCapture;
import cern.tim.driver.test.UseConf;
import cern.tim.driver.test.UseHandler;
import cern.tim.driver.tools.equipmentexceptions.EqCommandTagException;
import cern.tim.driver.tools.equipmentexceptions.EqIOException;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import cern.tim.shared.daq.datatag.SourceDataQuality;

/**
 * This class implements a set of JUnit tests for <code>JECMessageHandler</code>. All tests that require
 * JECMessageHandler pre-configuration with XML based configuration shall be annotated with <code>UseConf</code>
 * annotation, specifying the XML file to be used.
 * 
 * @see
 * @see ch.cern.tim.driver.dip.JECMessageHandler
 * @author wbuczak
 */
@UseHandler(JECMessageHandler.class)
public class JECMessageHandlerTest extends GenericMessageHandlerTst {
  
    static Logger log = Logger.getLogger(JECMessageHandlerTest.class);

    JECMessageHandler jecHandler;

    
    @Override
    protected void beforeTest() throws Exception {
        jecHandler = (JECMessageHandler) msgHandler;                
    }

    @Override
    protected void afterTest() throws Exception {
        jecHandler.disconnectFromDataSource();       
    }
    

//    @Test
//    @UseConf("e_jec_test1.xml")
//    public void test1() throws Exception {
//      
//        // TODO : create real tests !!
//        assertTrue(true);
//   }
   
}
