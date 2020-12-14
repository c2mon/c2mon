/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.client.core.jms.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnection;
import org.awaitility.Awaitility;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.client.core.config.JmsConfig;
import cern.c2mon.client.core.jms.JmsProxy;
import cern.c2mon.client.core.jms.TopicRegistrationDetails;
import cern.c2mon.client.core.listener.TagUpdateListener;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.request.JsonRequest;

/**
 * Unit test of JmsProxy implementation.
 * 
 * 
 * @author Mark Brightwell
 *
 */
public class JmsProxyImplTest {

  /**
   * Class to test.
   */
  private JmsProxy jmsProxy;
  
  private JmsConnectionHandler connetionHandler;
  
  /**
   * Mocks
   */
  ConnectionFactory connectionFactory;
  ActiveMQConnection connection;
  Session session;
  
  @Before
  public void setUp() {
    connectionFactory = EasyMock.createNiceMock(ConnectionFactory.class);
    connection = EasyMock.createNiceMock(ActiveMQConnection.class);
    session = EasyMock.createNiceMock(Session.class);
    SlowConsumerListener slowConsumerListener = EasyMock.createNiceMock(SlowConsumerListener.class);
    JmsConfig jmsConfig = new JmsConfig();
    ExecutorService topicPollingExecutor = jmsConfig.topicPollingExecutor();
    connetionHandler = new JmsConnectionHandler(connectionFactory, slowConsumerListener, topicPollingExecutor);
    
    jmsProxy = new JmsProxyImpl(connetionHandler, slowConsumerListener, topicPollingExecutor, new C2monClientProperties());
  }
  
  /**
   * Call registerUpdateListener with null.
   * @throws JMSException 
   */
  @Test(expected = NullPointerException.class)
  public void testRegisterNullUpdateListener() throws JMSException {
    jmsProxy.registerUpdateListener(null, EasyMock.createMock(TopicRegistrationDetails.class));
  }
  
  
  /**
   * Call registerUpdateListener with null details.
   * @throws JMSException 
   */
  @Test(expected = NullPointerException.class)
  public void testRegisterNullDetails() throws JMSException {
    jmsProxy.registerUpdateListener(EasyMock.createMock(TagUpdateListener.class), null);
  }
  
  /**
   * Call unregister with null.
   * @throws JMSException 
   */
  @Test(expected = NullPointerException.class)
  public void testUnRegisterNullListener() throws JMSException {
    jmsProxy.unregisterUpdateListener(null);
  }
  
  /**
   * Call unregister supervision with null.
   * @throws JMSException 
   */
  public void testUnRegisterNullSupervisionListener() throws JMSException {
    jmsProxy.unregisterSupervisionListener(null);
  }
  
  /**
   * Call unregister supervision with null.
   * @throws JMSException 
   */
  @Test(expected = NullPointerException.class)
  public void testRegisterNullSupervisionListener() throws JMSException {
    jmsProxy.registerSupervisionListener(null);
  }
  
  /**
   * Call register heartbeat with null.
   */
  @Test(expected = NullPointerException.class)
  public void testRegisterNullHeartbeatListener() throws JMSException {
    jmsProxy.registerHeartbeatListener(null);
  }
  
  /**
   * Call unregister heartbeat with null.
   */
  public void testUnregisterNullHeartbeatListener() throws JMSException {
    jmsProxy.unregisterHeartbeatListener(null);
  }
  
  /**
   * Call unregister supervision with null.
   * @throws JMSException 
   */
  @Test(expected = NullPointerException.class)
  public void testRegisterNullConnectionListener() throws JMSException {
    jmsProxy.registerConnectionListener(null);
  }
  
  /**
   * Test sendRequest with null request object - should throw exception.
   * Also calls the lifecycle start() method and checks connection and session
   * calls.
   * @throws JMSException
   * @throws InterruptedException 
   */
  @Test(expected = NullPointerException.class)
  public void testStartAndSendRequestNullRequest() throws JMSException, InterruptedException { 
    //need to simulate start
    MessageConsumer messageConsumer = simulateStart();
    
    Awaitility.await().atMost(2, TimeUnit.SECONDS)
    .until(() -> connetionHandler.isConnected());
    
    jmsProxy.sendRequest(null, "test.queue", 1000);
    EasyMock.verify(connectionFactory);
    EasyMock.verify(connection);
    EasyMock.verify(session);
    EasyMock.verify(messageConsumer);
  }

  private MessageConsumer simulateStart() throws JMSException {
    EasyMock.expect(connectionFactory.createConnection()).andReturn(connection).times(2);        
    EasyMock.expect(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).andReturn(session).times(3);
    connection.setExceptionListener(EasyMock.isA(ExceptionListener.class));  
    connection.start();       
    
    MessageConsumer messageConsumer = EasyMock.createMock(MessageConsumer.class);
    EasyMock.expect(session.createConsumer(EasyMock.isA(Destination.class))).andReturn(messageConsumer).times(3);    
    messageConsumer.setMessageListener(EasyMock.isA(MessageListener.class));
    messageConsumer.setMessageListener(EasyMock.isA(MessageListener.class));
    messageConsumer.setMessageListener(EasyMock.isA(MessageListener.class));
    session.close();
        
    EasyMock.replay(connectionFactory);
    EasyMock.replay(connection);
    EasyMock.replay(session);
    EasyMock.replay(messageConsumer);
    connetionHandler.ensureConnection();
    return messageConsumer;
  }
  
  /**
   * Test sendRequest with null queue name - should throw exception.
   * @throws JMSException
   * @throws InterruptedException 
   */
  @Test(expected = NullPointerException.class)
  public void testSendRequestNullQueue() throws JMSException, InterruptedException { 
    JsonRequest<ClientRequestResult> jsonRequest = EasyMock.createMock(JsonRequest.class);
    MessageConsumer messageConsumer = simulateStart();
    Thread.sleep(2000); //leave time for connection thread to run (and set connected flag to true)
    jmsProxy.sendRequest(jsonRequest, null, 1000);
    EasyMock.verify(connectionFactory);
    EasyMock.verify(connection);
    EasyMock.verify(session);
    EasyMock.verify(messageConsumer);
  }

}
