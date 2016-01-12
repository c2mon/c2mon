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
package cern.c2mon.daq.ens;

import static org.junit.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  
    static Logger log = LoggerFactory.getLogger(ENSMessageHandlerTest.class);

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
