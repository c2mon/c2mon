/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.monit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.util.parser.SimpleXMLParser;

/**
 * Checking the registration and unregistration of tags by the MessageHandler. In the Spectrum DAQ,
 * a big part of the specific business logic is in the SpectrumEventProcessor (test and the corresp.
 * test class). Connection and disconnection is also checked there. In consequence, the aspects
 * to be tests for the message handler are in register() and unregistrer();
 * 
 * @author mbuttner
 */
@UseHandler(MonitMessageHandler.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class MonitMessageHandlerTest extends GenericMessageHandlerTst {
    
    Logger LOG = LoggerFactory.getLogger(MonitMessageHandlerTest.class);
    protected static MonitMessageHandler theHandler;
    

    
    //
    // --- TEST --------------------------------------------------------------------------------
    //    
    @UseConf("spectrum_test_1.xml")
    @Test
    public void testRegistration() throws ParserConfigurationException {
        MonitEventProcessor proc = theHandler.getProcessor();
        ChangeReport changes = new ChangeReport(1L);
        SimpleXMLParser parser = new SimpleXMLParser();
        
        String xml = MonitTestUtil.getConfigTag("cs-ccr-diam1", 1L);
        SourceDataTag t1 = SourceDataTag.fromConfigXML(parser.parse(xml).getDocumentElement());
        theHandler.onAddDataTag(t1, changes);
        assertTrue(changes.isSuccess());
        MonitUpdateEvent alarm = proc.getAlarm("cs-CCR-diaM1");
        assertNotNull(alarm);

        theHandler.onRemoveDataTag(t1, changes);
        assertTrue(changes.isSuccess());
        alarm = proc.getAlarm("cs-CCR-diaM1");
        assertNull(alarm);
                
        LOG.info("Test completed.");
    }

    @UseConf("spectrum_test_1.xml")
    @Test
    public void testReplacement() throws ParserConfigurationException {
        MonitEventProcessor proc = theHandler.getProcessor();
        ChangeReport changes = new ChangeReport(1L);
        SimpleXMLParser parser = new SimpleXMLParser();
        
        String xml = MonitTestUtil.getConfigTag("cs-ccr-diam1", 1L);
        SourceDataTag t1 = SourceDataTag.fromConfigXML(parser.parse(xml).getDocumentElement());
        theHandler.onAddDataTag(t1, changes);
        assertTrue(changes.isSuccess());
        MonitUpdateEvent alarm = proc.getAlarm("cs-CCR-diaM1");
        assertNotNull(alarm);

        xml = MonitTestUtil.getConfigTag("cs-ccr-dmnp1", 1L);
        SourceDataTag t2 = SourceDataTag.fromConfigXML(parser.parse(xml).getDocumentElement());
        theHandler.onUpdateDataTag(t2, t1, changes);
        assertTrue(changes.isSuccess());
        assertNull(proc.getAlarm("cs-CCR-diaM1"));
        assertNotNull(proc.getAlarm("cs-ccr-dmnp1"));
                
        LOG.info("Test completed.");
    }
    
    //
    // --- SETUP --------------------------------------------------------------------------------
    //
    @Override
    protected void beforeTest() throws Exception {
        LOG.info("Init ...");
        System.setProperty("spectrum.mode", "junit");
        theHandler = (MonitMessageHandler) msgHandler;        
        MonitMessageHandler.profile = "TEST";
        theHandler.connectToDataSource();        
        LOG.info("Init done.");
    }

    @Override
    protected void afterTest() throws Exception {
        if (theHandler != null) {
            theHandler.shutdown();
        }
        LOG.info("Resources cleared.");
    }

}
