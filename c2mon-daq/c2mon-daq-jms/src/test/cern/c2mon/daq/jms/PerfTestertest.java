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
package cern.c2mon.daq.jms;

import javax.jms.JMSException;

import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.jms.PerfTester;

import static org.junit.Assert.*;


public class PerfTestertest {

    BrokerService broker = null;
    
    @Before
    public void before() {
        broker = new BrokerService();
        broker.setPersistent(false);
        broker.setDedicatedTaskRunner(false);
        broker.setUseJmx(false);
        broker.setUseShutdownHook(true);
    }
    
    @After
    public void after() {
        
    }
    
    @Test
    public void testSendAndReceive() throws Exception {
        PerfTester p = new PerfTester(broker.getVmConnectorURI().toString());
        assertTrue(p.canConnect());
        assertTrue(p.measureQueueMessagePerf("test") > 0);
        assertTrue(p.measureTopicMessagePerf("test") > 0);
    }
}
