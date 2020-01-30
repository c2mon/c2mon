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
package cern.c2mon.server.daq;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.impl.SimpleCache;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.daq.config.DaqModule;
import cern.c2mon.server.daq.update.JmsContainerManagerImpl;
import cern.c2mon.shared.util.jms.ActiveJmsSender;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Integration test of JmsContainerManager component with ActiveMQ broker.
 *
 * @author Mark Brightwell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheConfigModuleRef.class,
    CacheDbAccessModule.class,
    DaqModule.class,
    DaqModule.class,
})
public class JmsContainerManagerTest {

  /**
   * Component to test.
   */
  @Autowired
  private JmsContainerManagerImpl jmsContainerManager;

  /*
   * Mocks
   */
  private C2monCache<Process> mockProcessCache;
  private SessionAwareMessageListener mockListener;
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

  @Autowired
  @Qualifier("daqInConnectionFactory")
  private ConnectionFactory daqInConnectionFactory;

  @Before
  public void setUp() throws Exception {
    mockProcessCache = new SimpleCache<>("process");
    mockListener = EasyMock.createMock(SessionAwareMessageListener.class);

    jmsSender = new ActiveJmsSender();
    JmsTemplate template = new JmsTemplate();
    template.setConnectionFactory(daqInConnectionFactory);
    template.setTimeToLive(60000);
    jmsSender.setJmsTemplate(template);

//    jmsContainerManager = new JmsContainerManagerImpl(mockProcessCache, daqInConnectionFactory, mockListener, mockClusterCache, new StandardEnvironment());
//    jmsContainerManager.setConsumersInitial(1);
//    jmsContainerManager.setConsumersMax(1);
//    jmsContainerManager.setNbExecutorThreads(2);
//    jmsContainerManager.setIdleTaskExecutionLimit(1);
//    jmsContainerManager.setMaxMessages(1);
//    jmsContainerManager.setReceiveTimeout(100);
//    jmsContainerManager.setJmsUpdateQueueTrunk(testTrunkName);
//    jmsContainerManager.setSessionTransacted(true);
//    jmsContainerManager.setUpdateWarmUpSeconds(60);
  }

  /**
   * For tests that assume the container manager is started.
   */
  private void startContainerManager() {
    //init with no processes in cache; use mock cache to start correctly
    Assert.assertEquals(0, mockProcessCache.getKeys().size());
    jmsContainerManager.init();
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
  @Ignore("This test is broken")
  public void testInitAtStartUp() throws InterruptedException, JMSException {
    ArrayList<Long> keys = new ArrayList<Long>();
    keys.add(0, 1L);
    keys.add(1, 2L);
    Process mockProcess1 = EasyMock.createMock(Process.class);
    Process mockProcess2 = EasyMock.createMock(Process.class);
//    EasyMock.expect(mockProcessCache.getKeys()).andReturn(keys);
    EasyMock.expect(mockProcessCache.get(1L)).andReturn(mockProcess1);
    EasyMock.expect(mockProcessCache.get(2L)).andReturn(mockProcess2);
    long millis = System.currentTimeMillis();
    EasyMock.expect(mockProcess1.getName()).andReturn("Process-1-" + millis).times(2);
    EasyMock.expect(mockProcess2.getName()).andReturn("Process-2-" + millis).times(2);
    EasyMock.expect(mockProcess1.getId()).andReturn(1L);
    EasyMock.expect(mockProcess2.getId()).andReturn(2L);

    final CountDownLatch latch = new CountDownLatch(2);

    //expect one message from each
    mockListener.onMessage(EasyMock.isA(TextMessage.class), EasyMock.isA(Session.class));
    EasyMock.expectLastCall().andAnswer(() -> {
      latch.countDown();
      return null;
    });

    mockListener.onMessage(EasyMock.isA(TextMessage.class), EasyMock.isA(Session.class));
    EasyMock.expectLastCall().andAnswer(() -> {
      latch.countDown();
      return null;
    });

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

    // wait for the listeners to fire
    latch.await();

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
  @Ignore("This test is broken")
  public void testSubscribe() throws InterruptedException, JMSException {
    startContainerManager();
    mockProcess = EasyMock.createMock(Process.class);
    long millis = System.currentTimeMillis();
    EasyMock.expect(mockProcess.getId()).andReturn(10L).times(3);
    EasyMock.expect(mockProcess.getName()).andReturn("Process-3-" + millis).times(2);

    final CountDownLatch latch = new CountDownLatch(1);

    //expect one message from each
    mockListener.onMessage(EasyMock.isA(TextMessage.class), EasyMock.isA(Session.class));
    EasyMock.expectLastCall().andAnswer(() -> {
      latch.countDown();
      return null;
    });

    //run test
    EasyMock.replay(mockProcess);
    EasyMock.replay(mockListener);

    //init subscriptions
    jmsContainerManager.subscribe(mockProcess);
    //check messages are picked up
    jmsSender.sendToQueue("test subscribe to process 3", testTrunkName + ".Process-3-" + millis);

    // wait for the listeners to fire
    latch.await();

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
  @Ignore("This test is broken")
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

    //check that listener was NOT called
    EasyMock.verify(mockListener);
    EasyMock.verify(mockProcess);
  }
}
