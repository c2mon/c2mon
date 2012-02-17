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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cern.accsoft.commons.util.proc.ProcUtils;
import cern.accsoft.commons.util.proc.ProcessInfo;
import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.client.jms.AdminMessageListener;
import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.client.jms.ConnectionListener;
import cern.c2mon.client.jms.HeartbeatListener;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.SupervisionListener;
import cern.c2mon.client.jms.TopicRegistrationDetails;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.request.JsonRequest;
import cern.tim.shared.client.command.CommandExecuteRequest;

/**
 * Implementation of the JmsProxy singleton bean. Also see the interface
 * for documentation.
 * 
 * <p>A new session (thread) is created for every Topic subscription. For this
 * reason, the number of different topics across all Tags should be
 * kept to a reasonable number (say around 50) and adjusted upwards if the number
 * of incoming updates is causing performance problems. These sessions are created when
 * needed and closed when possible (i.e. when no more update listeners registered on 
 * the topic).
 * 
 * <p>Separate sessions are used for listening to supervision events and sending
 * requests to the server (the latter created at request time).
 * 
 * <p>Notice this component requires an explicit start of the Spring context (or direct call to the
 * start method).
 * 
 * <p>Connect and disconnect methods are synchronized to prevent them running in parallel.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public final class JmsProxyImpl implements JmsProxy, ExceptionListener {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(JmsProxyImpl.class);

  /**
   * Time between reconnection attempts if the first attempt fails (in milliseconds).
   */
  private static final long SLEEP_BETWEEN_CONNECTION_ATTEMPTS = 5000;
  
  /**
   * Timeout used for all messages sent to the server: notice this needs to be quite large
   * to account for unsynchronized clients.
   */
  private static final int JMS_MESSAGE_TIMEOUT = 600000;
  
  /**
   * The JMS connection factory.
   */
  private ConnectionFactory jmsConnectionFactory;
  
  /**
   * The unique JMS connection used.
   */
  private Connection connection;
      
  /**
   * Indicates which {@link MessageListenerWrapper} is listening to a given topic
   * (each wrapper listens to a single topic). 
   */
  private Map<String, MessageListenerWrapper> topicToWrapper;
  
  /**
   * Points to the JMS Session started for a given wrapper. Only the sessions
   * used for Tag update subscriptions are referenced here.
   */
  private Map<MessageListenerWrapper, Session> sessions;
  
  /**
   * 1-1 correspondence between ServerUpdateListeners and the Tags they are
   * receiving updates for (usually both are the same object).
   */
  private Map<TagUpdateListener, TopicRegistrationDetails> registeredListeners;
  
  /**
   * Listener locks, to prevent concurrent subscription/unsubscription
   * of a given listener.
   */
  private Map<TagUpdateListener, ReentrantReadWriteLock.WriteLock> listenerLocks;
  
  /**
   * Listeners that need informing about JMS connection and disconnection
   * events.
   */
  private Collection<ConnectionListener> connectionListeners; 
  private ReentrantReadWriteLock connectionListenersLock;
  
  /**
   * Subscribes to the Supervision topic and notifies any registered listeners. 
   */
  private SupervisionListenerWrapper supervisionListenerWrapper;
  
  /**
   * Subscribes to the admin messages topic and notifies any registered listeners. 
   */
  private AdminMessageListenerWrapper adminMessageListenerWrapper;
  
  /**
   *  Subscribes to the Supervision topic and notifies any registered listeners.
   */
  private HeartbeatListenerWrapper heartbeatListenerWrapper;
  
  /**
   *  Subscribes to the alarm topic and notifies any registered listeners.
   */
  private AlarmListenerWrapper alarmListenerWrapper;
  
  /**
   *  Alarm Session.
   */
  private Session alarmSession;                    
  
  /**
   *  Alarm Consumer.
   */  
  private MessageConsumer alarmConsumer;

  /**
   * Recording connection status to JMS.
   * Only set in connect and startReconnectThread methods.
   */
  private volatile boolean connected;
  
  /**
   * Track start/stop status.
   */
  private volatile boolean running;
  
  /**
   * A final shutdown of this JmsProxy has been requested.
   */
  private volatile boolean shutdownRequested;
  
  /**
   * A lock used to prevent multiple reconnection threads being
   * started.
   */
  private ReentrantReadWriteLock.WriteLock connectingWriteLock;
  
  /**
   * No refresh of the subscriptions is allowed at the same
   * time as registrations or unregistrations, since a refresh
   * involves cleaning all the internal maps and re-subscribing
   * consumers on the various sessions.
   */
  private ReentrantReadWriteLock refreshLock;

  /**
   * Topic on which supervision events arrive from server.
   */
  private Destination supervisionTopic;
  
  /**
   * Topic on which admin messages are being sent, and on which it arrives
   */
  private Destination adminMessageTopic;
    
  /**
   * Topic on which server heartbeat messages are arriving.
   */
  private Destination heartbeatTopic;
  
  /**
   * Topic on which server alarm messages are arriving.
   */
  private Destination alarmTopic;
  
  /**
   * Constructor.
   * @param connectionFactory the JMS connection factory
   * @param supervisionTopic topic on which supervision events arrive from server
   * @param alarmTopic topic on which alarm messages arrive from server
   * @param heartbeatTopic topic on which heartbeat messages arrive from server
   */
  @Autowired
  public JmsProxyImpl(final ConnectionFactory connectionFactory, 
      @Qualifier("supervisionTopic") final Destination supervisionTopic,
      @Qualifier("alarmTopic") final Destination alarmTopic,
      @Qualifier("heartbeatTopic") final Destination heartbeatTopic) {
    this.jmsConnectionFactory = connectionFactory;
    this.supervisionTopic = supervisionTopic;
    this.heartbeatTopic = heartbeatTopic;
    this.alarmTopic = alarmTopic;
    this.adminMessageTopic = null;
    
    
    connected = false;
    running = false;
    shutdownRequested = false;    
    connectingWriteLock = new ReentrantReadWriteLock().writeLock();
    refreshLock = new ReentrantReadWriteLock();
    sessions = new ConcurrentHashMap<MessageListenerWrapper, Session>();    
    topicToWrapper = new ConcurrentHashMap<String, MessageListenerWrapper>();
    registeredListeners = new ConcurrentHashMap<TagUpdateListener, TopicRegistrationDetails>();
    listenerLocks = new ConcurrentHashMap<TagUpdateListener, ReentrantReadWriteLock.WriteLock>();
    connectionListeners = new ArrayList<ConnectionListener>(); 
    connectionListenersLock = new ReentrantReadWriteLock();
    supervisionListenerWrapper = new SupervisionListenerWrapper();
    adminMessageListenerWrapper = new AdminMessageListenerWrapper();
    heartbeatListenerWrapper = new HeartbeatListenerWrapper();
    alarmListenerWrapper = new AlarmListenerWrapper();
  }
  
  /**
   * Sets the prefix id on the JMS connection factory.
   * Only works if JMS connection factory is ActiveMQ.
   */
  private void setActiveMQConnectionPrefix() {
    ProcessInfo procInfo = ProcUtils.get().getProcessInfo();
    String clientIdPrefix = "C2MON-CLIENT-" + procInfo.getUserId() + "@" + procInfo.getHostName() 
        + "[" + procInfo.getPid() + "]"; 
    ((ActiveMQConnectionFactory) jmsConnectionFactory).setClientIDPrefix(clientIdPrefix);
  }

  /**
   * Runs until (re)connected. Should only be called by one thread.
   * Use startReconnectThread to run in another thread (only one
   * thread will be started however many calls are made).
   * 
   * <p>Listeners are notified of connection once the connection
   * is reestablished and all topic subscriptions are back.
   */
  private synchronized void connect() {
      while (!connected && !shutdownRequested) {
        try {                    
          connection = jmsConnectionFactory.createConnection();         
          refreshSubscriptions();          
          connection.setExceptionListener(this);
          connection.start();
          connected = true;
        } catch (Exception e) {
          LOGGER.error("Exception caught while trying to refresh the JMS connection; sleeping 5s before retrying.", e);
          try {
            Thread.sleep(SLEEP_BETWEEN_CONNECTION_ATTEMPTS);
          } catch (InterruptedException interEx) {
            LOGGER.error("InterruptedException caught while waiting to reconnect.", interEx);
          }
        }
      }
      if (connected) {
        notifyConnectionListenerOnConnection();        
      }      
  }
  
  /**
   * Notifies all {@link ConnectionListener}s of a connection. 
   */
  private void notifyConnectionListenerOnConnection() {
    connectionListenersLock.readLock().lock();
    try {
      for (ConnectionListener listener : connectionListeners) {
        listener.onConnection();
      }      
    } finally {
      connectionListenersLock.readLock().unlock();
    }    
  }
  
  /**
   * Notifies all {@link ConnectionListener}s on a disconnection.
   */
  private void notifyConnectionListenerOnDisconnection() {
    connectionListenersLock.readLock().lock();
    try {
      for (ConnectionListener listener : connectionListeners) {
        listener.onDisconnection();
      }
    } finally {
      connectionListenersLock.readLock().unlock();
    }    
  }

  /**
   * Also notifies listeners of disconnection.
   */
  private synchronized void disconnect() {
    connected = false;
    notifyConnectionListenerOnDisconnection();
    try {
      connection.close(); //closes all consumers and sessions also
    } catch (JMSException jmsEx) {
      LOGGER.error("disconnect() - Exception caught while attempting to disconnect from JMS - aborting this attempt.", jmsEx);
    } 
  }
  
  /**
   * Only starts the thread if it is not already started
   * (thread will always be started if connected has been
   * set to false).
   */
  private void startReconnectThread() {
    //lock to prevent multiple threads from starting (before connected flag is modified)
    connectingWriteLock.lock();
    try {
      if (connected) {
        disconnect(); //notifies listeners       
        new Thread(new Runnable() {                  
          public void run() {
            connect();
          }
        }).start();
      }
    } finally {
     connectingWriteLock.unlock();     
    }
    
  }
  
  /**
   * Refreshes all the Topic subscriptions. Assumes previous connection/sessions
   * are now inactive.
   * 
   * @throws JMSException if problem subscribing
   */
  private void refreshSubscriptions() throws JMSException {
    refreshLock.writeLock().lock();    
    try {    
      if (!registeredListeners.isEmpty()) {        
        sessions.clear();
        topicToWrapper.clear();      
        //refresh all registered listeners for Tag updates      
        for (Map.Entry<TagUpdateListener, TopicRegistrationDetails> entry : registeredListeners.entrySet()) {          
          registerUpdateListener(entry.getKey(), entry.getValue());
        }        
      } 
      //refresh supervision subscription
      subscribeToSupervisionTopic();
      subscribeToHeartbeatTopic();
      subscribeToAdminMessageTopic();
    } catch (JMSException e) {
      LOGGER.error("Did not manage to refresh Topic subscriptions.", e);
      throw e;
    } finally {
      refreshLock.writeLock().unlock();
    }
  }
  
  /**
   * Subscribes to the alarm topic. 
   */
  private void subscribeToAlarmTopic() throws JMSException {
    alarmSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);                           
    alarmConsumer = alarmSession.createConsumer(alarmTopic);                 
    alarmConsumer.setMessageListener(alarmListenerWrapper);
    LOGGER.debug("Successfully subscribed to alarm topic");
  }
  
  private void unsubscribeFromAlarmTopic() throws JMSException {
    
    alarmSession.close();
    alarmSession = null;
    alarmConsumer.close();
    alarmConsumer = null;
    LOGGER.debug("Successfully unsubscribed from alarm topic");
  }

  /**
   * Subscribes to the heartbeat topic. Called when refreshing
   * all subscriptions.
   */
  private void subscribeToHeartbeatTopic() throws JMSException {
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);                           
    MessageConsumer consumer = session.createConsumer(heartbeatTopic);                 
    consumer.setMessageListener(heartbeatListenerWrapper);
  }

  /**
   * Called when refreshing subscriptions at start up and again
   * if the connection goes down.
   * 
   * @throws JMSException if unable to subsribe
   */
  private void subscribeToAdminMessageTopic() throws JMSException {
    if (adminMessageTopic != null) {
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      final MessageConsumer consumer = session.createConsumer(adminMessageTopic);
      consumer.setMessageListener(adminMessageListenerWrapper);
    }
  }
  
  /**
   * Called when refreshing subscriptions at start up and again
   * if the connection goes down.
   * 
   * @throws JMSException if unable to subsribe
   */
  private void subscribeToSupervisionTopic() throws JMSException {   
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer consumer = session.createConsumer(supervisionTopic);                 
    consumer.setMessageListener(supervisionListenerWrapper);           
  }

  @Override
  public boolean isRegisteredListener(final TagUpdateListener serverUpdateListener) {
    if (serverUpdateListener == null) {
      throw new NullPointerException("isRegisteredListener() method called with null parameter!");
    }   
    return registeredListeners.containsKey(serverUpdateListener);    
  }

  @Override
  public void registerUpdateListener(final TagUpdateListener serverUpdateListener, 
                            final TopicRegistrationDetails topicRegistrationDetails) throws JMSException {   
    refreshLock.readLock().lock();
    try {
      if (topicRegistrationDetails == null) {
        throw new NullPointerException("Trying to register a TagUpdateListener with null RegistrationDetails!");
      }
      if (!listenerLocks.containsKey(serverUpdateListener)) {
        listenerLocks.put(serverUpdateListener, new ReentrantReadWriteLock().writeLock());
      }
      listenerLocks.get(serverUpdateListener).lock();
      try {
        if (!isRegisteredListener(serverUpdateListener)) { //throw exception if TagUpdateListener null
          try {
            if (connected) {
              String topicName = topicRegistrationDetails.getTopicName();
              if (topicToWrapper.containsKey(topicName)) {
                topicToWrapper.get(topicName).addListener(serverUpdateListener, topicRegistrationDetails.getId());
              } else {                
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);              
                Topic topic = session.createTopic(topicRegistrationDetails.getTopicName());
                MessageConsumer consumer = session.createConsumer(topic);         
                MessageListenerWrapper wrapper = new MessageListenerWrapper(topicRegistrationDetails.getId(), serverUpdateListener);
                consumer.setMessageListener(wrapper);
                topicToWrapper.put(topicName, wrapper);
                sessions.put(wrapper, session);
              }                             
              registeredListeners.put(serverUpdateListener, topicRegistrationDetails);
            } else {           
              throw new JMSException("Not currently connected - will attempt to subscribe on reconnection. Attempting to reconnect.");     
            }
          } catch (JMSException e) {
            LOGGER.error("Failed to subscribe to topic - will do so on reconnection.", e);
            registeredListeners.put(serverUpdateListener, topicRegistrationDetails);
            throw e;
          }
        } else {
          LOGGER.debug("Update listener already registered; skipping registration (for Tag " + topicRegistrationDetails.getId() + ")");     
        }
      } finally {
        listenerLocks.get(serverUpdateListener).unlock();
      }      
    } finally {
      refreshLock.readLock().unlock();
    }    
  }

  @Override
  public void replaceListener(final TagUpdateListener registeredListener, final TagUpdateListener replacementListener) {
    if (registeredListener == null && replacementListener == null) {
      throw new NullPointerException("replaceListener(..) method called with null argument");
    }
    refreshLock.readLock().lock();
    try {
      if (!listenerLocks.containsKey(registeredListener)) {
        throw new IllegalStateException("Tried to replace a unrecognized update listener.");
      }
      ReentrantReadWriteLock.WriteLock listenerLock = listenerLocks.get(registeredListener);
      listenerLock.lock();      
      try {
        TopicRegistrationDetails tag = registeredListeners.get(registeredListener); 
        topicToWrapper.get(tag.getTopicName()).addListener(replacementListener, tag.getId());       
        listenerLocks.put(replacementListener, listenerLocks.remove(registeredListener));
        registeredListeners.put(replacementListener, registeredListeners.remove(registeredListener));
      } finally {
        listenerLock.unlock();        
      }
    } finally {
      refreshLock.readLock().unlock();
    }    
  }

  @Override
  public void publish(final String message, final String topicName, final long timeToLive) throws JMSException {
    if (topicName == null) {
      throw new NullPointerException("publish(..) method called with null queue name argument");
    }
    if (message == null) {
      throw new NullPointerException("publish(..) method called with null message argument");
    }
    if (connected) {
      final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      try {
        final Message messageObj = session.createTextMessage(message);
  
        final MessageProducer producer = session.createProducer(new ActiveMQTopic(topicName));
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        producer.setTimeToLive(JMS_MESSAGE_TIMEOUT);
        producer.send(messageObj);
      }
      finally {
        session.close();
      }
    }
    else {
      throw new JMSException("Not currently connected: unable to send message at this time.");
    }
  }
  
  /**
   * ActiveMQ-specific implementation since need to create topic.
   * @return a Collection of ClientRequestResults
   */
  @Override
  public <T extends ClientRequestResult> Collection<T> sendRequest(final JsonRequest<T> jsonRequest, 
                                                   final String queueName, final int timeout) throws JMSException {  
    if (queueName == null) {
      throw new NullPointerException("sendRequest(..) method called with null queue name argument");
    }
    if (jsonRequest == null) {
      throw new NullPointerException("sendRequest(..) method called with null request argument");
    }
    if (connected) {
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      try {
        
        Message message = null;
        
        if (jsonRequest.isObjectRequest()) { // used for EXECUTE_COMMAND_REQUESTS
          
          // send only the object
          CommandExecuteRequest o = (CommandExecuteRequest) jsonRequest.getObjectParameter();
          message = session.createObjectMessage(o);

        }
        else { // used for all other request types
          
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
          Message replyMessage = consumer.receive(timeout);
          if (replyMessage == null) {
            LOGGER.error("No reply received from server on ClientRequest. I was waiting for " + timeout + " milliseconds..");          
            throw new RuntimeException("No reply received from server - possible timeout?");
          }
          
          if (replyMessage instanceof ObjectMessage) {
            
            return (Collection<T>)((ObjectMessage) replyMessage).getObject() ;
          }
          
          else // replyMessage is an instanceof TextMessage

            return jsonRequest.fromJsonResponse(((TextMessage) replyMessage).getText()); 
          
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
 
  @Override
  public void unregisterUpdateListener(final TagUpdateListener serverUpdateListener) {
    refreshLock.readLock().lock();
    try {
      if (!listenerLocks.containsKey(serverUpdateListener)) {
        throw new IllegalStateException("Tried to unregister an unrecognized update listener.");
      }
      ReentrantReadWriteLock.WriteLock listenerLock = listenerLocks.get(serverUpdateListener);
      listenerLock.lock();
      try {
        if (isRegisteredListener(serverUpdateListener)) {
          TopicRegistrationDetails subsribedToTag = registeredListeners.get(serverUpdateListener);
          MessageListenerWrapper wrapper = topicToWrapper.get(subsribedToTag.getTopicName());
          wrapper.removeListener(subsribedToTag.getId());
          if (wrapper.isEmpty()) { //no subscribed listeners, so close session
            try {            
              Session session = sessions.get(wrapper);
              session.close();            
            } catch (JMSException ex) {
              LOGGER.error("Failed to unregister properly from a Tag update; subscriptions will be refreshed.");            
              startReconnectThread();            
            } finally {
              sessions.remove(wrapper);            
              topicToWrapper.remove(subsribedToTag.getTopicName());              
            }
          }
          registeredListeners.remove(serverUpdateListener);
          listenerLocks.remove(serverUpdateListener);
        } 
      } finally {
        listenerLock.unlock();
      }            
    } finally {
      refreshLock.readLock().unlock();
    }       
  }

  @Override
  public void registerConnectionListener(final ConnectionListener connectionListener) {
    if (connectionListener == null) {
      throw new NullPointerException("registerConnectionListener(..) method called with null listener argument");
    }
    connectionListenersLock.writeLock().lock();
    try {
      connectionListeners.add(connectionListener);
    } finally {
      connectionListenersLock.writeLock().unlock();
    }    
  }

  /**
   * Listeners are notified of disconnection in reconnection thread.
   */
  @Override
  public void onException(final JMSException exception) {
    LOGGER.error("JMSException caught by JMS connection exception listener. Attempting to reconnect.", exception);
    startReconnectThread();
  }
  
  @Override
  public void registerSupervisionListener(final SupervisionListener supervisionListener) {
    if (supervisionListener == null) {
      throw new NullPointerException("Trying to register null Supervision listener with JmsProxy.");
    }
    supervisionListenerWrapper.addListener(supervisionListener);           
  }

  @Override
  public void unregisterSupervisionListener(final SupervisionListener supervisionListener) { 
    if (supervisionListener == null) {
      throw new NullPointerException("Trying to unregister null Supervision listener from JmsProxy.");
    }
    supervisionListenerWrapper.removeListener(supervisionListener);        
  }
  
  @Override
  public void registerAdminMessageListener(final AdminMessageListener adminMessageListener) {
    if (adminMessageListener == null) {
      throw new NullPointerException("Trying to register null AdminMessage listener with JmsProxy.");
    }
    if (adminMessageTopic == null) {
      throw new IllegalStateException(String.format("Cannot register '%s' without having the admin message topic", 
          AdminMessageListener.class.getSimpleName()));
    }
    adminMessageListenerWrapper.addListener(adminMessageListener);           
  }

  @Override
  public void unregisterAdminMessageListener(final AdminMessageListener adminMessageListener) { 
    if (adminMessageListener == null) {
      throw new NullPointerException("Trying to unregister null AdminMessage listener from JmsProxy.");
    }
    adminMessageListenerWrapper.removeListener(adminMessageListener);        
  }
  
  @Override
  public void registerAlarmListener(final AlarmListener alarmListener) throws JMSException {
    if (alarmListener == null) {
      throw new NullPointerException("Trying to register null alarm listener with JmsProxy.");
    }
    if (alarmListenerWrapper.getListenerCount() == 0) { // this is our first listener!
      // -> it's time to subscribe to the alarm topic
      try {
        subscribeToAlarmTopic();
      }catch (JMSException e) {
        LOGGER.error("Did not manage to subscribe To Alarm Topic.", e);
        throw e;
      } 
    }
    alarmListenerWrapper.addListener(alarmListener);
  }
  
  @Override
  public void unregisterAlarmListener(final AlarmListener alarmListener) throws JMSException {
    if (alarmListener == null) {
      throw new NullPointerException("Trying to unregister null alarm listener from JmsProxy.");
    }
    
    if (alarmListenerWrapper.getListenerCount() == 1) { // this is our last listener!
      // -> it's time to unsubscribe from the topic
      try {
        unsubscribeFromAlarmTopic();
      }catch (JMSException e) {
        LOGGER.error("Did not manage to subscribe To Alarm Topic.", e);
        throw e;
      } 
    }
    
    alarmListenerWrapper.removeListener(alarmListener);
  }

  @Override
  public void registerHeartbeatListener(final HeartbeatListener heartbeatListener) {
    if (heartbeatListener == null) {
      throw new NullPointerException("Trying to register null Heartbeat listener with JmsProxy.");
    }
    heartbeatListenerWrapper.addListener(heartbeatListener);
  }

  @Override
  public void unregisterHeartbeatListener(final HeartbeatListener heartbeatListener) {
    if (heartbeatListener == null) {
      throw new NullPointerException("Trying to unregister null Heartbeat listener from JmsProxy.");
    }
    heartbeatListenerWrapper.removeListener(heartbeatListener);
  }
  
  @Override
  public synchronized void setAdminMessageTopic(final Destination adminMessageTopic) {
    if (this.adminMessageTopic != null) {
      throw new IllegalStateException("Cannot set the admin message topic more than one time");
    }
    this.adminMessageTopic = adminMessageTopic;
    if (this.adminMessageTopic != null && this.connected) {
      try {
        subscribeToAdminMessageTopic();
      }
      catch (JMSException e) {
        LOGGER.error("Unable to subscribe to the admin message topic, this functionality may not function properly.", e);
      }
    }
  }

  @PostConstruct
  public void init() {    
    //thread starting connection; is stopped when calling shutdown method
    new Thread(new Runnable() {      
      @Override
      public void run() {
        setActiveMQConnectionPrefix();
        connect();
      }
    }).start();
    running = true;
  }
  
  @PreDestroy
  public void stop() {
    shutdownRequested = true;
    connectionListenersLock.writeLock().lock();
    try {
      connectionListeners.clear();
    } finally {
      connectionListenersLock.writeLock().unlock();
    }  
    disconnect();
    running = false;
  }

}
