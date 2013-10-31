/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
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
