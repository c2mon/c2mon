/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import javax.jms.*;

import org.apache.activemq.command.ActiveMQQueue;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.client.core.config.C2monAutoConfiguration;
import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.client.core.config.mock.MockServerConfig;
import cern.c2mon.client.core.jms.ConnectionListener;
import cern.c2mon.client.core.jms.JmsProxy;
import cern.c2mon.client.core.jms.SupervisionListener;
import cern.c2mon.client.core.jms.TopicRegistrationDetails;
import cern.c2mon.client.core.listener.TagUpdateListener;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.request.*;
import cern.c2mon.shared.client.serializer.TransferTagSerializer;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import cern.c2mon.shared.util.jms.ActiveJmsSender;
import cern.c2mon.shared.util.json.GsonFactory;

/**
 * Integration testing of JmsProxy implementation with ActiveMQ broker.
 *
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    C2monAutoConfiguration.class,
    MockServerConfig.class
})
@TestPropertySource(
    properties = {
        "c2mon.client.jms.url=vm://localhost:61616?broker.persistent=false&broker.useShutdownHook=false&broker.useJmx=false"
    }
)
public class JmsProxyTest {

  /**
   * Component to test.
   */
  @Autowired
  private JmsProxy jmsProxy;
  
  @Autowired
  private JmsConnectionHandler topicPollingExecutor;

  @Autowired
  private C2monClientProperties properties;

  /**
   * For sending message to the broker, to be picked up by the tested proxy.
   */
  @Autowired
  private ActiveJmsSender jmsSender;

  @Autowired
  private JmsTemplate serverTemplate;

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
    JsonRequest<SupervisionEvent> jsonRequest = new ClientRequestImpl<>(SupervisionEvent.class);
    final String queueName = properties.getJms().getRequestQueue() + "-" + System.currentTimeMillis();
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
            Collection<SupervisionEvent> supervisionEvents = new ArrayList<>();
            supervisionEvents.add(new SupervisionEventImpl(SupervisionEntity.PROCESS, 1L, "P_TEST", SupervisionStatus.RUNNING, new Timestamp(System.currentTimeMillis()), "test response"));
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

    jmsProxy.registerUpdateListener(listener, details);

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

    //expect
    EasyMock.expect(listener.onUpdate(EasyMock.isA(TransferTagValueImpl.class))).andReturn(true);

    //run test
    EasyMock.replay(listener);

    //register listener
    jmsProxy.registerUpdateListener(listener, details);
    //send update
    jmsSender.sendToTopic(TransferTagSerializer.toJson(new TransferTagValueImpl(details.getId(),
                                                   10L,
                                                   "value description",
                                                   new DataTagQualityImpl(),
                                                   TagMode.TEST,
                                                   new Timestamp(System.currentTimeMillis()),
                                                   new Timestamp(System.currentTimeMillis()),
                                                   new Timestamp(System.currentTimeMillis()),
                                                   "description")),
                            details.getTopicName());

    //pause and verify
    Thread.sleep(200);
    EasyMock.verify(listener);

  }



  /**
   * Tests unregistration to a topic works.
   * @throws JMSException
   */
  @Test
  public void testUnregisterUpdateListener() throws JMSException {
    TagUpdateListener listener = EasyMock.createMock(TagUpdateListener.class);

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
  public void testSupervisionNotification() throws InterruptedException, JMSException {
    SupervisionEvent event = new SupervisionEventImpl(SupervisionEntity.EQUIPMENT, 10L, "P_TEST",
                                      SupervisionStatus.DOWN, new Timestamp(System.currentTimeMillis()), "test event");
    CountDownLatch latch = new CountDownLatch(2);
    jmsProxy.registerUpdateListener(tagValueUpdate -> false, details);

    jmsProxy.registerSupervisionListener(supervisionEvent -> latch.countDown());
    jmsProxy.registerSupervisionListener(supervisionEvent -> latch.countDown());
    String topicName = properties.getJms().getSupervisionTopic();
    Assert.assertNotNull(topicName);
    jmsSender.sendToTopic(((SupervisionEventImpl) event).toJson(), topicName);

    //wait for message
    latch.await();
  }

  /**
   * Tests reconnect works when onException is called and that registered
   * connection listeners are notified correctly. Test runs with both supervision and update
   * listeners registered and check if reconnected afterwards.
   * @throws JMSException
   * @throws InterruptedException
   */
  @Test
  public void testReconnectAndNotification() throws JMSException, InterruptedException {
    CountDownLatch latch = new CountDownLatch(3);

    jmsProxy.registerConnectionListener(new ConnectionListener() {
      public void onConnection() { latch.countDown(); }
      public void onDisconnection() { latch.countDown(); }
    });
    topicPollingExecutor.getConnection().getTransport().getTransportListener().transportInterupted();

    latch.await();

    //check connection is back by rerunning supervison and update tests
    testSupervisionNotification();
    testUpdateNotification();
  }

  /**
   * Tests client requests with intermediate progress reports are processed and notify listener
   * correctly. Fakes server response with some progress reports followed by the result.
   * @throws JMSException
   */
  @Test
  public void testProgressReportProcessing() throws JMSException {
    ClientRequestReportListener reportListener = EasyMock.createMock(ClientRequestReportListener.class);
    reportListener.onProgressReportReceived(EasyMock.isA(ClientRequestProgressReport.class));
    EasyMock.expectLastCall().times(3);

    EasyMock.replay(reportListener);

    ClientRequestImpl<ConfigurationReport> jsonRequest = new ClientRequestImpl<>(ConfigurationReport.class);
    final String queueName = properties.getJms().getRequestQueue() + "-" + System.currentTimeMillis();
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

            //send progress reports
            MessageProducer producer = session.createProducer(message.getJMSReplyTo());
            Collection<ConfigurationReport> configReport = new ArrayList<>();
            configReport.add(new ConfigurationReport(10, 5, 20, 2, "fake progress"));
            Message replyMessage = session.createTextMessage(GsonFactory.createGson().toJson(configReport));
            producer.send(replyMessage);

            configReport.clear();
            configReport.add(new ConfigurationReport(1, 1, 2, 1, "fake progress"));
            replyMessage = session.createTextMessage(GsonFactory.createGson().toJson(configReport));
            producer.send(replyMessage);

            configReport.clear();
            configReport.add(new ConfigurationReport(10, 6, 22, 10, "fake progress"));
            replyMessage = session.createTextMessage(GsonFactory.createGson().toJson(configReport));
            producer.send(replyMessage);

            //send result
            configReport.clear();
            configReport.add(new ConfigurationReport(10L, "name", "user"));
            replyMessage = session.createTextMessage(GsonFactory.createGson().toJson(configReport));
            producer.send(replyMessage);
            return null;
          }

        }, true);
      }
    }).start();
    Collection<ConfigurationReport> response = jmsProxy.sendRequest(jsonRequest, queueName, 10000, reportListener); //wait 10s for an answer
    Assert.assertNotNull(response);
    Assert.assertTrue(response.iterator().next().isResult());

    EasyMock.verify(reportListener);

  }

  /**
   * Tests error reports are correctly processed. Fakes a sequence of progress reports back from the
   * server followed by an error report.
   *
   * @throws JMSException
   */
  @Test
  public void testErrorReportProcessing() throws JMSException {
    ClientRequestReportListener reportListener = EasyMock.createMock(ClientRequestReportListener.class);
    reportListener.onProgressReportReceived(EasyMock.isA(ClientRequestProgressReport.class));
    EasyMock.expectLastCall().times(3);
    reportListener.onErrorReportReceived(EasyMock.isA(ClientRequestErrorReport.class));

    EasyMock.replay(reportListener);

    ClientRequestImpl<ConfigurationReport> jsonRequest = new ClientRequestImpl<>(ConfigurationReport.class);
    final String queueName = properties.getJms().getRequestQueue() + "-" + System.currentTimeMillis();
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

            //send progress reports
            MessageProducer producer = session.createProducer(message.getJMSReplyTo());
            Collection<ConfigurationReport> configReport = new ArrayList<>();
            configReport.add(new ConfigurationReport(10, 5, 20, 2, "fake progress"));
            Message replyMessage = session.createTextMessage(GsonFactory.createGson().toJson(configReport));
            producer.send(replyMessage);

            configReport.clear();
            configReport.add(new ConfigurationReport(1, 1, 2, 1, "fake progress"));
            replyMessage = session.createTextMessage(GsonFactory.createGson().toJson(configReport));
            producer.send(replyMessage);

            configReport.clear();
            configReport.add(new ConfigurationReport(10, 6, 22, 10, "fake progress"));
            replyMessage = session.createTextMessage(GsonFactory.createGson().toJson(configReport));
            producer.send(replyMessage);

            //send result
            configReport.clear();
            configReport.add(new ConfigurationReport(false, "error occurred"));
            replyMessage = session.createTextMessage(GsonFactory.createGson().toJson(configReport));
            producer.send(replyMessage);
            return null;
          }

        }, true);
      }
    }).start();
    boolean exceptionCaught = false;
    Collection<ConfigurationReport> response = null;
    try {
      response = jmsProxy.sendRequest(jsonRequest, queueName, 10000, reportListener); //wait 10s for an answer
    } catch (RuntimeException e) {
      exceptionCaught = true;
      assertTrue(e.getMessage().endsWith("error occurred"));
    }
    assertTrue(exceptionCaught);
    assertNull(response);

    EasyMock.verify(reportListener);

  }

}
