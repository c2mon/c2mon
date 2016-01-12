/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

package cern.c2mon.daq.monit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
import cern.c2mon.shared.util.parser.SimpleXMLParser;

/**
 * TODO problem timestamps (buffer, update, )
 * 
 * Check add/remove/get of a host into the internal data structure of the processr
 * Check that one minute without backup or backup with NOT OK ends in connection not ok
 * 
 * approach: create events, put events on queue, check that event processor establishes correct situation
 *  * 
 * Scenario 2:
 * - send activate
 * - check tha alarm is active
 * - send terminate
 * - check that alarm is terminated
 * ...
 * 
 * @author mbuttner
 */
@UseHandler(MonitMessageHandler.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MonitEventProcessorTest extends GenericMessageHandlerTst {

    Logger LOG = LoggerFactory.getLogger(MonitEventProcessorTest.class);
    protected static MonitMessageHandler theHandler;
    
    private static final String primaryServer = "cs-srv-44.cern.ch";
    private static final String secondaryServer = "cs-srv-45.cern.ch";

    
    //
    // --- TEST --------------------------------------------------------------------------------
    //    
    /*
     * Check add and remove hosts to/from event processor, and subsequent retrieving of config
     * data
     * 
     */
    @UseConf("spectrum_test_1.xml")
    @Test
    public void testInternalDataStruct() throws ParserConfigurationException {
        MonitEventProcessor proc = theHandler.getProcessor();
        SimpleXMLParser parser = new SimpleXMLParser();
        
        String xml = MonitTestUtil.getConfigTag("cs-ccr-diam1", 1L);
        SourceDataTag tag = SourceDataTag.fromConfigXML(parser.parse(xml).getDocumentElement());
//        proc.add("cs-ccr-diam1.cern.ch", tag);
        
        MonitUpdateEvent alarm = proc.getAlarm("cs-CCR-diaM1");
        assertNotNull(alarm);
        
        proc.del("cs-ccr-diam1");
        alarm = proc.getAlarm("cs-ccr-diam1");
        assertNull(alarm);
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
        MonitTestUtil.trySleepSec(3);
        LOG.info("Init done.");
        MonitTestUtil.trySleepSec(3);
    }

    @Override
    protected void afterTest() throws Exception {
        if (theHandler != null) {
            theHandler.shutdown();
        }
    }

}
