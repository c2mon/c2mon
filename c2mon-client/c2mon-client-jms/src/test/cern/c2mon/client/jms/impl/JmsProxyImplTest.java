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
package cern.c2mon.client.jms.impl;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.SmartLifecycle;

import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.TopicRegistrationDetails;
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
  
  /**
   * Mocks
   */
  ConnectionFactory connectionFactory;
  Connection connection;
  Session session;
  
  @Before
  public void setUp() {
    connectionFactory = EasyMock.createMock(ConnectionFactory.class);
    connection = EasyMock.createMock(Connection.class);
    session = EasyMock.createMock(Session.class);
    Destination supervisionTopic = EasyMock.createMock(Destination.class);
    jmsProxy = new JmsProxyImpl(connectionFactory, supervisionTopic);
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
  @Test(expected = NullPointerException.class)
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
    EasyMock.expect(connectionFactory.createConnection()).andReturn(connection).times(2);        
    EasyMock.expect(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).andReturn(session);
    connection.setExceptionListener(EasyMock.isA(ExceptionListener.class));  
    connection.start();
    EasyMock.expect(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).andReturn(session);    
    
    MessageConsumer messageConsumer = EasyMock.createMock(MessageConsumer.class);
    EasyMock.expect(session.createConsumer(EasyMock.isA(Destination.class))).andReturn(messageConsumer);
    session.close();
        
    EasyMock.replay(connectionFactory);
    EasyMock.replay(connection);
    EasyMock.replay(session);
    ((SmartLifecycle) jmsProxy).start();
    Thread.sleep(2000); //leave time for connection thread to run (and set connected flag to true)
    jmsProxy.sendRequest(null, "test.queue", 1000);
    EasyMock.verify(connectionFactory);
    EasyMock.verify(connection);
    EasyMock.verify(session);
  }
  
  /**
   * Test sendRequest with null queue name - should throw exception.
   * @throws JMSException
   * @throws InterruptedException 
   */
  @Test(expected = NullPointerException.class)
  public void testSendRequestNullQueue() throws JMSException, InterruptedException { 
    JsonRequest<ClientRequestResult> jsonRequest = EasyMock.createMock(JsonRequest.class);
    //need to simulate start
    EasyMock.expect(connectionFactory.createConnection()).andReturn(connection);    
    EasyMock.expect(connection.createSession(false, Session.SESSION_TRANSACTED)).andReturn(session);    
        
    EasyMock.replay(connectionFactory);
    EasyMock.replay(connection);
    ((SmartLifecycle) jmsProxy).start();
    Thread.sleep(2000); //leave time for connection thread to run (and set connected flag to true)
    jmsProxy.sendRequest(jsonRequest, null, 1000);
    EasyMock.verify(connectionFactory);
    EasyMock.verify(connection);
  }
  
}
