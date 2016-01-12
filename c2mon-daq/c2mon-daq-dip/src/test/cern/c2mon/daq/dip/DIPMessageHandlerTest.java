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
package cern.c2mon.daq.dip;

import static org.junit.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  
    static Logger log = LoggerFactory.getLogger(DIPMessageHandlerTest.class);

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
