/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.daq.jms;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.network.NetworkConnector;
import org.apache.log4j.Logger;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;

import cern.c2mon.daq.jms.BrokerConfig;
import cern.c2mon.daq.jms.JMSMessageHandler;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.SourceDataTagValueCapture;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;

@UseHandler(JMSMessageHandler.class)
public class JmsMessageHandlerTest extends GenericMessageHandlerTst {

    static Logger log = Logger.getLogger(JmsMessageHandlerTest.class);
    
    JMSMessageHandler jmsHandler;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    @Override
    protected void afterTest() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    protected void beforeTest() throws Exception {
        log.info("entering beforeTest()..");
        jmsHandler = (JMSMessageHandler) msgHandler;
        jmsHandler.autoStart = false;

    }

    @Test
    @UseConf("e_jms_test2.xml")
    public void testConfig() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        replay(messageSender);

        jmsHandler.connectToDataSource();
        
        Thread.sleep(1000);

        verify(messageSender);

        //assertEquals(2, jmsHandler.brokersList.size());
        //assertEquals(1, jmsHandler.bridgedBrokers.size());
    }
    
    @Test
    @UseConf("e_jms_test2.xml")
    public void testBrokerCluster() throws Exception {
        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(4);
        
        replay(messageSender);
        
        BrokerService broker = new BrokerService();
        broker.setPersistent(false);
        broker.setDedicatedTaskRunner(false);
        broker.setBrokerName("localtest1");
        broker.setUseJmx(false);
        broker.setUseShutdownHook(false);
        broker.setSystemExitOnShutdown(true);
        broker.addConnector("tcp://localhost:61616");
        broker.start();
        
        BrokerService broker2 = new BrokerService();
        broker2.setPersistent(false);
        broker2.setBrokerName("localtest2");
        broker2.setUseJmx(false);
        broker2.setUseShutdownHook(false);
        broker2.setSystemExitOnShutdown(true);
        broker2.addConnector("tcp://localhost:61617");
        
        NetworkConnector nc = broker2.addNetworkConnector(new URI("static://(tcp://localhost:61616)"));
        nc.setDuplex(true);
        nc.setNetworkTTL(2);
        
        broker2.start();
        
        jmsHandler.connectToDataSource();
        jmsHandler.runCheck();
        
        Thread.sleep(1000);

        verify(messageSender);


        assertEquals(1, sdtv.getNumberOfCapturedValues(54676L));
        assertEquals(1, sdtv.getNumberOfCapturedValues(54677L));
        assertEquals(1, sdtv.getNumberOfCapturedValues(54611L));
        assertEquals(1, sdtv.getNumberOfCapturedValues(54612L));
    }
    
    
    
    @Test(expected=EqIOException.class)
    @UseConf("e_failingHWAdress.xml")
    public void testFailedHWAddress() throws Exception {

      jmsHandler.connectToDataSource();
   
    }
    
    
    @Test
    @UseConf("e_failingConnection.xml")
    public void testConnectionFailed() throws Exception {
        
        Capture<Long> id = new Capture<Long>();
        Capture<Boolean> val = new Capture<Boolean>();
        Capture<String> msg = new Capture<String>();
             
        /*
         * the commFaultTag is set to Ok at the beginning, so expect the call.
         */
        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(2);

        replay(messageSender);

        jmsHandler.connectToDataSource();
        jmsHandler.runCheck();
     
        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(1, sdtv.getNumberOfCapturedValues(54676L));
        assertEquals(1, sdtv.getNumberOfCapturedValues(54677L));
        
        
        assertEquals(SourceDataQuality.DATA_UNAVAILABLE, sdtv.getLastValue(54676L).getQuality().getQualityCode());
        assertEquals("Cannot aquire queue message perf for 'tcp://I-do-not-exist:61661 ': Could not connect to broker URL: tcp://I-do-not-exist:61661. Reason: java.net.UnknownHostException: I-do-not-exist", sdtv.getLastValue(54676L).getQuality().getDescription());

        assertEquals(SourceDataQuality.DATA_UNAVAILABLE, sdtv.getLastValue(54677L).getQuality().getQualityCode());
        assertEquals("Cannot aquire topic message perf for 'tcp://I-do-not-exist:61661 ': Could not connect to broker URL: tcp://I-do-not-exist:61661. Reason: java.net.UnknownHostException: I-do-not-exist", sdtv.getLastValue(54677L).getQuality().getDescription());        
        
    }
    
    
    @Test
    @UseConf("e_jms_test2.xml")
    public void testSimpleDataTagAddress() {
            
        BrokerConfig g;
        try {
            g = JMSMessageHandler.generateConfig(equipmentConfiguration);
            System.out.println(g);
        } catch (EqIOException e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
    }
}
