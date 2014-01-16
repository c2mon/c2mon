/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.daqcommunication.in;

import java.util.ArrayList;
import java.util.Collections;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.SmartLifecycle;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.daqcommunication.in.update.JmsContainerManagerImpl;
import cern.c2mon.server.test.broker.TestBrokerService;
import cern.c2mon.shared.util.jms.ActiveJmsSender;

/**
 * Integration test of JmsContainerManager component with ActiveMQ broker (rest
 * of server core dependencies are mocked).
 * 
 * <p>Imports connection XML and properties import XML.
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({ "classpath:cern/c2mon/server/daqcommunication/in/config/server-daqcommunication-in-connection.xml",
                        "classpath:cern/c2mon/server/test/server-import-properties.xml"})
public class JmsContainerManagerTest {

  /**
   * Component to test.
   */
  private JmsContainerManagerImpl jmsContainerManager;
  
  /*
   * Mocks
   */
  private ProcessCache mockProcessCache;
  private SessionAwareMessageListener mockListener;
  private ClusterCache mockClusterCache;
  //for tests that share a process and queue name
  private Process mockProcess;
  private String tmpQueueName;
  
  /**
   * Test queue trunk name.
   */
  private String testTrunkName = "c2mon.server.jmscontainermanager.test";
  
  /**
   * Sender used for testing messages are picked up.
   */
  private ActiveJmsSender jmsSender;
  
  /**
   * Connection to JMS instantiated in XML.
   */  
  private ConnectionFactory daqInConnectionFactory;
  
  private TestBrokerService testBrokerService;
  
  @Before
  public void setUp() throws Exception {    
    //start in-memory test broker
    testBrokerService = new TestBrokerService();
    testBrokerService.createAndStartBroker();
    daqInConnectionFactory = testBrokerService.getConnectionFactory();
    
    mockProcessCache = EasyMock.createMock(ProcessCache.class);
    mockListener = EasyMock.createMock(SessionAwareMessageListener.class);
    mockClusterCache = EasyMock.createMock(ClusterCache.class);
        
    jmsSender = new ActiveJmsSender();
    JmsTemplate template = new JmsTemplate();
    template.setConnectionFactory(daqInConnectionFactory);
    template.setTimeToLive(60000);
    jmsSender.setJmsTemplate(template);
    
    jmsContainerManager = new JmsContainerManagerImpl(mockProcessCache, daqInConnectionFactory, mockListener, mockClusterCache); 
    jmsContainerManager.setConsumersInitial(1);
    jmsContainerManager.setConsumersMax(1);
    jmsContainerManager.setNbExecutorThreads(2);
    jmsContainerManager.setIdleTaskExecutionLimit(1);
    jmsContainerManager.setMaxMessages(1);
    jmsContainerManager.setReceiveTimeout(100);
    jmsContainerManager.setJmsUpdateQueueTrunk(testTrunkName);
    jmsContainerManager.setSessionTransacted(true);
    jmsContainerManager.setUpdateWarmUpSeconds(60);
  }
  
  @After
  public void stopBroker() throws Exception {
    testBrokerService.stopBroker();
  }
  
  /**
   * For tests that assume the container manager is started.
   */
  private void startContainerManager() {
    //init with no processes in cache; use mock cache to start correctly
    EasyMock.expect(mockProcessCache.getKeys()).andReturn(Collections.EMPTY_LIST);
    EasyMock.replay(mockProcessCache);
    ((JmsContainerManagerImpl) jmsContainerManager).init();
    ((SmartLifecycle) jmsContainerManager).start();
    EasyMock.reset(mockProcessCache);
  }
  /**
   * Tests that all containers are subscribed at start-up to the
   * correct JMS queues (by sending message to expected subscribed).
   * @throws InterruptedException 
   * @throws JMSException 
   */
  @Test
  public void testInitAtStartUp() throws InterruptedException, JMSException {
    ArrayList<Long> keys = new ArrayList<Long>();
    keys.add(0, 1L);
    keys.add(1, 2L);
    Process mockProcess1 = EasyMock.createMock(Process.class);
    Process mockProcess2 = EasyMock.createMock(Process.class);
    EasyMock.expect(mockProcessCache.getKeys()).andReturn(keys);
    EasyMock.expect(mockProcessCache.get(1L)).andReturn(mockProcess1);
    EasyMock.expect(mockProcessCache.get(2L)).andReturn(mockProcess2);
    long millis = System.currentTimeMillis();
    EasyMock.expect(mockProcess1.getName()).andReturn("Process-1-" + millis).times(2);
    EasyMock.expect(mockProcess2.getName()).andReturn("Process-2-" + millis).times(2);
    EasyMock.expect(mockProcess1.getId()).andReturn(1L);
    EasyMock.expect(mockProcess2.getId()).andReturn(2L);
    //expect one message from each
    mockListener.onMessage(EasyMock.isA(TextMessage.class), EasyMock.isA(Session.class));
    mockListener.onMessage(EasyMock.isA(TextMessage.class), EasyMock.isA(Session.class));
    
    //run test
    EasyMock.replay(mockProcessCache);
    EasyMock.replay(mockProcess1);
    EasyMock.replay(mockProcess2);
    EasyMock.replay(mockListener);
    
    //init subscriptions
    ((JmsContainerManagerImpl) jmsContainerManager).init();
    ((SmartLifecycle) jmsContainerManager).start();
    //check messages are picked up    
    jmsSender.sendToQueue("test message from process 1", testTrunkName + ".Process-1-" + millis);
    jmsSender.sendToQueue("test message from process 2", testTrunkName + ".Process-2-" + millis);
    
    //check after pause
    Thread.sleep(2000);
    EasyMock.verify(mockProcessCache);
    EasyMock.verify(mockProcess1);
    EasyMock.verify(mockProcess2);
    EasyMock.verify(mockListener);    
  }
  
  /**
   * Tests a single subscription works.
   * @throws InterruptedException 
   * @throws JMSException 
   */
  @Test
  public void testSubscribe() throws InterruptedException, JMSException {
    startContainerManager();
    mockProcess = EasyMock.createMock(Process.class); 
    long millis = System.currentTimeMillis();
    EasyMock.expect(mockProcess.getId()).andReturn(10L).times(3);
    EasyMock.expect(mockProcess.getName()).andReturn("Process-3-" + millis).times(2);
    //expect one message from each
    mockListener.onMessage(EasyMock.isA(TextMessage.class), EasyMock.isA(Session.class));
    
    //run test   
    EasyMock.replay(mockProcess);    
    EasyMock.replay(mockListener);
    
    //init subscriptions
    jmsContainerManager.subscribe(mockProcess);
    //check messages are picked up    
    jmsSender.sendToQueue("test subscribe to process 3", testTrunkName + ".Process-3-" + millis);    
    
    //check after pause
    Thread.sleep(2000);    
    EasyMock.verify(mockProcess);   
    EasyMock.verify(mockListener);
    
    //for unsubscribe test
    tmpQueueName = testTrunkName + ".Process-3-" + millis;
  }
  
  /**
   * Tests unsubscription from a Process.
   * 
   * <p>Runs subscription test first and accesses process and queue name
   * through global variables.
   * 
   * @throws InterruptedException 
   * @throws JMSException 
   */
  @Test
  public void testUnsubscribe() throws InterruptedException, JMSException {
    testSubscribe();
    EasyMock.reset(mockListener);
    EasyMock.reset(mockProcess);
    EasyMock.expect(mockProcess.getId()).andReturn(10L);

    //run test   
    EasyMock.replay(mockProcess);
    EasyMock.replay(mockListener);    
    jmsContainerManager.unsubscribe(mockProcess);
    //check messages are picked up    
    jmsSender.sendToQueue("test unsubscribe from process 3", tmpQueueName);    
    
    //check after pause that listener was NOT called
    Thread.sleep(2000);       
    EasyMock.verify(mockListener);
    EasyMock.verify(mockProcess);
  }
  
}
