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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.annotation.PreDestroy;
import javax.jms.*;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.google.gson.JsonSyntaxException;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.client.common.admin.BroadcastMessage;
import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.client.core.jms.*;
import cern.c2mon.client.core.listener.TagUpdateListener;
import cern.c2mon.shared.client.request.ClientRequestReport;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.request.JsonRequest;
import cern.c2mon.shared.client.supervision.Heartbeat;
import cern.c2mon.shared.client.supervision.SupervisionEvent;

/**
 * Implementation of the JmsProxy singleton bean. Also see the interface for
 * documentation.
 * <p>
 * Notice this component requires an explicit start of the Spring context (or
 * direct call to the start method).
 *
 * @author Mark Brightwell
 */
@Slf4j
@Component("jmsProxy")
@ManagedResource(objectName = "cern.c2mon:type=JMS,name=JmsProxy")
public final class JmsProxyImpl implements JmsProxy, JmsSubscriptionHandler {

  /**
   * Timeout used for all messages sent to the server: notice this needs to be
   * quite large to account for unsynchronized clients.
   */
  private static final int JMS_MESSAGE_TIMEOUT = 600000;
  
  private final JmsConnectionHandler jmsConnectionHandler;
  
  private final AbstractTopicWrapper<HeartbeatListener, Heartbeat> heartbeatTopicWrapper;

  private final AbstractTopicWrapper<SupervisionListener, SupervisionEvent> supervisionTopicWrapper;

  private final AbstractTopicWrapper<BroadcastMessageListener, BroadcastMessage> broadcastTopicWrapper;

  private final AlarmTopicWrapper alarmTopicWrapper;

  @Autowired
  public JmsProxyImpl(final JmsConnectionHandler jmsConnectionHandler,
                      final SlowConsumerListener slowConsumerListener,
                      @Qualifier("topicPollingExecutor") final ExecutorService topicPollingExecutor,
                      final C2monClientProperties properties) {

    this.jmsConnectionHandler = jmsConnectionHandler;
    jmsConnectionHandler.setJmsSubscriptionHandler(this);
    
    heartbeatTopicWrapper = new HeartbeatTopicWrapper(slowConsumerListener, topicPollingExecutor, properties);
    supervisionTopicWrapper = new SupervisionTopicWrapper(slowConsumerListener, topicPollingExecutor, properties);
    broadcastTopicWrapper = new BroadcastTopicWrapper(slowConsumerListener, topicPollingExecutor, properties);
    alarmTopicWrapper = new AlarmTopicWrapper(slowConsumerListener, topicPollingExecutor, properties);
  }
  
  @Override
  public void refreshAllSubscriptions(Connection connection) throws JMSException {
    if (alarmTopicWrapper.getListenerWrapper().getListenerCount() > 0) {
      alarmTopicWrapper.subscribeToTopic(connection);
    }
    supervisionTopicWrapper.subscribeToTopic(connection);
    heartbeatTopicWrapper.subscribeToTopic(connection);
    broadcastTopicWrapper.subscribeToTopic(connection);
  }

  @Override
  public boolean isRegisteredListener(final TagUpdateListener serverUpdateListener) {
    return jmsConnectionHandler.isRegisteredListener(serverUpdateListener);
  }

  @Override
  public void replaceListener(final TagUpdateListener registeredListener, final TagUpdateListener replacementListener) {
    jmsConnectionHandler.replaceListener(registeredListener, replacementListener);
  }

  @Override
  public void publish(final String message, final String topicName, final long timeToLive) throws JMSException {
    if (topicName == null) {
      throw new NullPointerException("publish(..) method called with null queue name argument");
    }
    if (message == null) {
      throw new NullPointerException("publish(..) method called with null message argument");
    }
    if (jmsConnectionHandler.isConnected()) {
      final Session session = jmsConnectionHandler.getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
      try {
        final Message messageObj = session.createTextMessage(message);

        final MessageProducer producer = session.createProducer(new ActiveMQTopic(topicName));
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        producer.setTimeToLive(JMS_MESSAGE_TIMEOUT);
        producer.send(messageObj);
      } finally {
        session.close();
      }
    } else {
      throw new JMSException("Not currently connected: unable to send message at this time.");
    }
  }

  @Override
  public <T extends ClientRequestResult> Collection<T> sendRequest(
      final JsonRequest<T> jsonRequest, final String queueName, final int timeout,
      final ClientRequestReportListener reportListener) throws JMSException {

    if (queueName == null) {
      throw new NullPointerException("sendRequest(..) method called with null queue name argument");
    }
    if (jsonRequest == null) {
      throw new NullPointerException("sendRequest(..) method called with null request argument");
    }

    jmsConnectionHandler.ensureConnection();

    if (jmsConnectionHandler.isConnected()) {
      Session session = jmsConnectionHandler.getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
      try {

        Message message = null;

        if (jsonRequest.isObjectRequest()) { 
          // used for EXECUTE_COMMAND_REQUESTS
          // send only the object
          message = session.createObjectMessage((Serializable) jsonRequest.getObjectParameter());
        } else { 
          // used for all other request types
          // send the Client Request as a Json Text Message
          message = session.createTextMessage(jsonRequest.toJson());
        }

        TemporaryQueue replyQueue = session.createTemporaryQueue();
        MessageConsumer consumer = session.createConsumer(replyQueue);
        try {
          message.setJMSReplyTo(replyQueue);
          MessageProducer producer = session.createProducer(new ActiveMQQueue(queueName));
          producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
          producer.setTimeToLive(JMS_MESSAGE_TIMEOUT);
          producer.send(message);

          while (jmsConnectionHandler.isConnected() && !jmsConnectionHandler.isShutdownRequested()) { 
            // until we receive the result
            // (it is possible to receive progress and / or error reports during this process)

            Message replyMessage = consumer.receive(timeout);
            if (replyMessage == null) {
              log.error("No reply received from server on ClientRequest. I was waiting for " + timeout + " milliseconds..");
              throw new RuntimeException("No reply received from server - possible timeout?");
            }

            if (replyMessage instanceof ObjectMessage) {
              return (Collection<T>) ((ObjectMessage) replyMessage).getObject();
            } else {
              // replyMessage is an instanceof TextMessage (json)
              TextMessage textMessage = (TextMessage) (replyMessage);

              Collection<T> resultCollection = handleJsonResponse(textMessage, jsonRequest, reportListener);
              if (resultCollection != null) {
                return resultCollection;
              }
            }
          }
          throw new RuntimeException("Disconnected from JMS, so unable to process request.");
        } finally {
          if (consumer != null) {
            consumer.close();
          }
          replyQueue.delete();
        }
      } finally {
        session.close();
      }
    } else {
      throw new JMSException("Not currently connected: unable to send request at this time.");
    }
  }

  /**
   * ActiveMQ-specific implementation since need to create topic.
   * @return a Collection of ClientRequestResults
   */
  @Override
  public <T extends ClientRequestResult> Collection<T> sendRequest(
      final JsonRequest<T> jsonRequest, final String queueName, final int timeout)
      throws JMSException {
    // we don't care about reports!
    ClientRequestReportListener reportListener = null; 
    return sendRequest(jsonRequest, queueName, timeout, reportListener);
  }

  /**
   * In case a JsonResponse has been received.
   * This can either be the final Result or a Report on the progress of the request.
   * @param jsonMessage the received message.
   * @param jsonRequest the original request. useful to decode the message
   * @param reportListener informed in case a Report is received. Can be null
   * in case no one cares about the progress of this request.
   * @param <T> type returned depends on the ClientRequest type.
   * @return a Collection of ClientRequestResults.
   * @throws JMSException if problem subscribing
   */
  private <T extends ClientRequestResult> Collection<T> handleJsonResponse(
      final TextMessage jsonMessage, final JsonRequest<T> jsonRequest, final ClientRequestReportListener reportListener)
      throws JsonSyntaxException, JMSException {

    Collection<T> resultCollection = jsonRequest.fromJsonResponse(jsonMessage.getText());
    if (resultCollection.isEmpty()) {
      // if the result is empty ->  we cannot do much with it
      return resultCollection;
    }

    // lets take the first element and check if it is a report
    ClientRequestResult result = resultCollection.iterator().next(); 
    
    if ((result instanceof ClientRequestReport)) { 
      // this can either be a Report or the actual Result
      ClientRequestReport report = (ClientRequestReport) result;
      if (isResult(report)) {
        // received the result!
        return resultCollection;
      } else { 
        // received a report -> let's handle the report! still waiting for the result though
        handleJsonReportResponse(report, reportListener); 
        return null;
      }
    }
    return resultCollection;
  }

  /**
   * Informs the listener in case a report is received.
   * @param report the received report.
   * @param reportListener the listener to be informed. Can be null in case no one cares about this report.
   */
  private void handleJsonReportResponse(final ClientRequestReport report, final ClientRequestReportListener reportListener) {

    if (reportListener == null) { 
      // is someone waiting for the report?
      log.debug("handleJsonReportResponse(): Received a report, but no reportListener is registered. Ignoring..");
      return;
    }

    if (report.isErrorReport()) {
      log.debug("handleJsonReportResponse(): Received an error report. Informing listener.");
      reportListener.onErrorReportReceived(report);
      throw new RuntimeException("Error report received from server on client request: " + report.getErrorMessage());
    } else if (report.isProgressReport()) {
      log.debug("handleJsonReportResponse(): Received a progress report. Informing listener.");
      reportListener.onProgressReportReceived(report);
    } else {
      log.warn("handleJsonReportResponse(): Received a report of unknown type. Ignoring..");
    }
  }

  /**
   * The server's response can either be a report or the actual result.
   * @param clientRequestReport the response to be checked
   * @return True if the final result is received, false in case a report has been received.
   */
  private boolean isResult(final ClientRequestReport clientRequestReport) {
    return clientRequestReport.isResult();
  }
  
  @Override
  public void registerUpdateListener(TagUpdateListener serverUpdateListener, TopicRegistrationDetails topicRegistrationDetails) throws JMSException {
    jmsConnectionHandler.registerUpdateListener(serverUpdateListener, topicRegistrationDetails);
  }

  @Override
  public void unregisterUpdateListener(final TagUpdateListener serverUpdateListener) {
    jmsConnectionHandler.unregisterUpdateListener(serverUpdateListener);
  }

  @Override
  public void registerConnectionListener(final ConnectionListener connectionListener) {
    jmsConnectionHandler.registerConnectionListener(connectionListener);
  }

  @Override
  public void registerSupervisionListener(final SupervisionListener supervisionListener) {
    if (supervisionListener == null) {
      throw new NullPointerException("Trying to register null Supervision listener with JmsProxy.");
    }

    jmsConnectionHandler.ensureConnection();
    supervisionTopicWrapper.getListenerWrapper().addListener(supervisionListener);
  }

  @Override
  public void unregisterSupervisionListener(final SupervisionListener supervisionListener) {
    supervisionTopicWrapper.removeListener(supervisionListener);
  }

  @Override
  public void registerBroadcastMessageListener(final BroadcastMessageListener broadcastMessageListener) {
    broadcastTopicWrapper.addListener(broadcastMessageListener);
  }

  @Override
  public void unregisterBroadcastMessageListener(final BroadcastMessageListener broadcastMessageListener) {
    broadcastTopicWrapper.removeListener(broadcastMessageListener);
  }

  @Override
  public void registerAlarmListener(final AlarmListener alarmListener) throws JMSException {
    if (alarmListener == null) {
      throw new NullPointerException("Trying to register null alarm listener with JmsProxy.");
    }

    jmsConnectionHandler.ensureConnection();

    if (alarmTopicWrapper.getListenerWrapper().getListenerCount() == 0) { 
      // this is our first listener!
      // -> it's time to subscribe to the alarm topic
      try {
        alarmTopicWrapper.subscribeToTopic(jmsConnectionHandler.getConnection());
      } catch (JMSException e) {
        log.error("Did not manage to subscribe To Alarm Topic.", e);
        throw e;
      }
    }
    alarmTopicWrapper.addListener(alarmListener);
  }

  @Override
  public void unregisterAlarmListener(final AlarmListener alarmListener) {
    if (alarmListener == null) {
      throw new NullPointerException("Trying to unregister null alarm listener from JmsProxy.");
    }

    if (alarmTopicWrapper.getListenerWrapper().getListenerCount() == 1) { 
      // this is our last listener! -> it's time to unsubscribe from the topic
      try {
        alarmTopicWrapper.unsubscribeFromAlarmTopic();
      } catch (JMSException e) {
        log.error("Did not manage to unsubscribe from JMS Alarm Topic.", e);
      }
    }

    alarmTopicWrapper.removeListener(alarmListener);
  }

  @Override
  public void registerHeartbeatListener(final HeartbeatListener heartbeatListener) {
    if (heartbeatListener == null) {
      throw new NullPointerException("Trying to register null Heartbeat listener with JmsProxy.");
    }

    jmsConnectionHandler.ensureConnection();
    heartbeatTopicWrapper.addListener(heartbeatListener);
  }

  @Override
  public void unregisterHeartbeatListener(final HeartbeatListener heartbeatListener) {
    heartbeatTopicWrapper.removeListener(heartbeatListener);
  }

  /**
   * Shuts down the JmsProxy. Connection listeners are not notified.
   */
  @PreDestroy
  public void stop() {
    supervisionTopicWrapper.stop();
    alarmTopicWrapper.stop();
    broadcastTopicWrapper.stop();
    heartbeatTopicWrapper.stop();
  }

  @ManagedOperation(description = "Get size of current internal listener queues")
  public Map<String, Integer> getQueueSizes() {
    Map<String, Integer> returnMap = new HashMap<>();
    for (Map.Entry<String, MessageListenerWrapper> entry : jmsConnectionHandler.getTopicToWrapper().entrySet()) {
      returnMap.put(entry.getKey(), entry.getValue().getQueueSize());
    }

    returnMap.put(supervisionTopicWrapper.getTopic().toString(), supervisionTopicWrapper.getQueueSize());
    returnMap.put(alarmTopicWrapper.getTopic().toString(), alarmTopicWrapper.getQueueSize());
    returnMap.put(broadcastTopicWrapper.getTopic().toString(), broadcastTopicWrapper.getQueueSize());
    
    returnMap.put(heartbeatTopicWrapper.getTopic().toString(), heartbeatTopicWrapper.getQueueSize());
    return returnMap;
  }
}
