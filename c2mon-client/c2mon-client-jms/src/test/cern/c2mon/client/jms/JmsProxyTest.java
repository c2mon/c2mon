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
package cern.c2mon.client.jms;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import junit.framework.Assert;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.shared.client.request.ClientRequestImpl;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.request.JsonRequest;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.client.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.client.supervision.SupervisionConstants.SupervisionStatus;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.util.jms.ActiveJmsSender;
import cern.tim.util.json.GsonFactory;

/**
 * Integration testing of JmsProxy implementation with ActiveMQ broker.
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/client/jms/config/c2mon-client-jms.xml" })
public class JmsProxyTest {
  
  /**
   * Component to test.
   */
  @Autowired
  private JmsProxy jmsProxy;
  
  /**
   * For sending message to the broker, to be picked up by the tested proxy.
   */
  private ActiveJmsSender jmsSender;
  
  private JmsTemplate serverTemplate;
 
  /**
   * Starts context.
   * @throws InterruptedException 
   */
  @Before
  public void setUp() throws InterruptedException {
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(System.getProperty("c2mon.jms.user"), 
                                                                                System.getProperty("c2mon.jms.passwd"),
                                                                                System.getProperty("c2mon.jms.url")); 
    jmsSender = new ActiveJmsSender();
    jmsSender.setJmsTemplate(new JmsTemplate(connectionFactory));
    serverTemplate = new JmsTemplate(connectionFactory);    
    //JMS connection is started in separate thread, so leave time to connect
    Thread.sleep(2000);
  }
  
  /**
   * Tests the sendRequest method throw the correct exception when the server does
   * no respond. Will also fail if the client-jms Spring context fails to load.
   * @throws JMSException 
   * @throws InterruptedException 
   */
  @Test(expected = RuntimeException.class)
  public void testSendRequestNoReply() throws JMSException {     
    JsonRequest<ClientRequestResult> jsonRequest = EasyMock.createMock(JsonRequest.class);
    EasyMock.expect(jsonRequest.toJson()).andReturn("{}");
    jmsProxy.sendRequest(jsonRequest, "random.no.reply.queue", 1000); //wait 1s for an answer
  }
  
  
  /**
   * Tests the sending of a request to the server and receiving a response
   * (decodes response and checks non null).
   * 
   * Start a new thread to mimick the server response to a client request.
   * @throws JMSException 
   * @throws InterruptedException 
   */
  @Test
  public void testSendRequest() throws JMSException, InterruptedException {     
    JsonRequest<SupervisionEvent> jsonRequest = new ClientRequestImpl<SupervisionEvent>(SupervisionEvent.class);      
    final String queueName = System.getProperty("c2mon.client.jms.request.queue") + "-" + System.currentTimeMillis();
    new Thread(new Runnable() {      
      @Override
      public void run() {       
        serverTemplate.execute(new SessionCallback<Object>() {

          @Override
          public Object doInJms(Session session) throws JMSException {            
            MessageConsumer consumer = session.createConsumer(new ActiveMQQueue(queueName));
            Message message = consumer.receive(10000);            
            Assert.assertNotNull(message);
            Assert.assertTrue(message instanceof TextMessage);
            //send some response (empty collection)
            Collection<SupervisionEvent> supervisionEvents = new ArrayList<SupervisionEvent>();
            supervisionEvents.add(new SupervisionEventImpl(SupervisionEntity.PROCESS, 1L, SupervisionStatus.RUNNING, new Timestamp(System.currentTimeMillis()), "test response"));
            Message replyMessage = session.createTextMessage(GsonFactory.createGson().toJson(supervisionEvents));
            MessageProducer producer = session.createProducer(message.getJMSReplyTo());
            producer.send(replyMessage);
            return null;
          }
         
        }, true);
      }
    }).start();     
    Collection<SupervisionEvent> response = jmsProxy.sendRequest(jsonRequest, queueName, 10000); //wait 10s for an answer
    Assert.assertNotNull(response);    
  }
  
  /**
   * Tests registration to a topic works.
   * @throws JMSException 
   */
  @Test
  public void testRegisterUpdateListener() throws JMSException {
    TagUpdateListener listener = EasyMock.createMock(TagUpdateListener.class);
    TopicRegistrationDetails details = EasyMock.createMock(TopicRegistrationDetails.class);
    EasyMock.expect(details.getTopicName()).andReturn("c2mon.JmsProxy.test.topic.registration");
    EasyMock.expect(details.getTopicName()).andReturn("c2mon.JmsProxy.test.topic.registration");
    EasyMock.expect(details.getId()).andReturn(1L);
    
    EasyMock.replay(listener);
    EasyMock.replay(details);
    jmsProxy.registerUpdateListener(listener, details);    
    EasyMock.verify(listener);
    EasyMock.verify(details);
    Assert.assertTrue(jmsProxy.isRegisteredListener(listener));
  }
  
  /**
   * Tests notifications are made to registered update listeners by sending JMS message.
   * (values are not checked, just notification).
   * @throws JMSException 
   * @throws InterruptedException 
   */
  @Test
  public void testUpdateNotification() throws JMSException, InterruptedException {
    TagUpdateListener listener = EasyMock.createMock(TagUpdateListener.class);
    TopicRegistrationDetails details = new TopicRegistrationDetails() {
      
      @Override
      public String getTopicName() {
        return "c2mon.JmsProxy.test.topic.registration";
      }
      
      @Override
      public Long getId() {
        return 1L;
      }
    };
    
    //expect
    listener.onUpdate(EasyMock.isA(TransferTagValueImpl.class));
    
    
    //run test
    EasyMock.replay(listener);
    
    //register listener
    jmsProxy.registerUpdateListener(listener, details);
    //send update
    jmsSender.sendToTopic(new TransferTagValueImpl(details.getId(), 
                                                   10L, 
                                                   new DataTagQualityImpl(), 
                                                   new Timestamp(System.currentTimeMillis()), 
                                                   new Timestamp(System.currentTimeMillis()), 
                                                   "description").toJson(), 
                            details.getTopicName());
    
    //pause and verify
    Thread.sleep(1000);
    EasyMock.verify(listener);

  }
  
   
  
  /**
   * Tests registration to a topic works.
   * @throws JMSException 
   */
  @Test
  public void testUnregisterUpdateListener() throws JMSException {
    TagUpdateListener listener = EasyMock.createMock(TagUpdateListener.class);
    TopicRegistrationDetails details = new TopicRegistrationDetails() {
      
      @Override
      public String getTopicName() {
        return "c2mon.JmsProxy.test.topic.registration";
      }
      
      @Override
      public Long getId() {
        return 1L;
      }
    };

    //register first
    jmsProxy.registerUpdateListener(listener, details);
    
    //run test: unregister      
    jmsProxy.unregisterUpdateListener(listener);
    
    //check results        
    Assert.assertFalse(jmsProxy.isRegisteredListener(listener));
  }
  
  /**
   * Tests registration to a topic works.
   * @throws JMSException 
   */
  @Test
  public void testReplaceUpdateListener() throws JMSException {
    TagUpdateListener listener = EasyMock.createMock(TagUpdateListener.class);
    TopicRegistrationDetails details = new TopicRegistrationDetails() {
      
      @Override
      public String getTopicName() {
        return "c2mon.JmsProxy.test.topic.registration";
      }
      
      @Override
      public Long getId() {
        return 1L;
      }
    };
    
    //new listener to replace old
    TagUpdateListener newListener = EasyMock.createMock(TagUpdateListener.class);
      
    //first register (already tested)
    jmsProxy.registerUpdateListener(listener, details);
    
    //run test - replace listener  
    jmsProxy.replaceListener(listener, newListener);
    
    //check results      
    Assert.assertFalse(jmsProxy.isRegisteredListener(listener));
    Assert.assertTrue(jmsProxy.isRegisteredListener(newListener));
  }
  
  /**
   * Tests that the JmsProxy receives supervision events from the broker and that 
   * registered {@link SupervisionListener}s are notified.
   * @throws InterruptedException
   */
  @Test
  @DirtiesContext
  public void testSupervisionNotification() throws InterruptedException {
    SupervisionListener supervisionListener1 = EasyMock.createMock(SupervisionListener.class);
    SupervisionListener supervisionListener2 = EasyMock.createMock(SupervisionListener.class);
    SupervisionEvent event = new SupervisionEventImpl(SupervisionEntity.EQUIPMENT, 10L, 
                                      SupervisionStatus.DOWN, new Timestamp(System.currentTimeMillis()), "test event");
   
    //expect
    supervisionListener1.onSupervisionUpdate(event);
    supervisionListener2.onSupervisionUpdate(event);
    
    //test (register listeners and publish supervision event)
    EasyMock.replay(supervisionListener1);
    EasyMock.replay(supervisionListener2);
    
    jmsProxy.registerSupervisionListener(supervisionListener1);
    jmsProxy.registerSupervisionListener(supervisionListener2);
    String topicName = System.getProperty("c2mon.client.jms.supervision.topic");
    Assert.assertNotNull(topicName);
    jmsSender.sendToTopic(((SupervisionEventImpl) event).toJson(), topicName);
    
    //wait for message
    Thread.sleep(1000);
    
    //verify
    EasyMock.verify(supervisionListener1);
    EasyMock.verify(supervisionListener2);
  }
  
  /**
   * Tests reconnect works when onException is called and that registered
   * connection listeners are notified correcly. Test runs with both supervision and update
   * listeners registered and check if reconnected afterwards.
   * @throws JMSException 
   * @throws InterruptedException 
   */
  @Test
  public void testReconnectAndNotification() throws JMSException, InterruptedException {   
    ConnectionListener connectionListener = EasyMock.createMock(ConnectionListener.class);    
    jmsProxy.registerConnectionListener(connectionListener);
    
    //expect
    connectionListener.onDisconnection();
    connectionListener.onConnection();
    
    //test (throw exception, which should disconnect and reconnect)
    EasyMock.replay(connectionListener);
    ((ExceptionListener) jmsProxy).onException(new JMSException("test exception handling"));
    //give time to disconnect and reconnect
    Thread.sleep(5000);
    
    //verify
    EasyMock.verify(connectionListener);
    
    //check connection is back by rerunning supervison and update tests
    testSupervisionNotification();
    testUpdateNotification();
  }
  
}
