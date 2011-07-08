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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.log4j.Logger;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.c2mon.client.jms.ConnectionListener;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.ServerUpdateListener;
import cern.c2mon.client.jms.SupervisionListener;
import cern.c2mon.client.jms.TopicRegistrationDetails;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.request.JsonRequest;

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
 * @author Mark Brightwell
 *
 */
@Service
public final class JmsProxyImpl implements JmsProxy, ExceptionListener, SmartLifecycle {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(JmsProxyImpl.class);
  
  /**
   * Number of JMS sessions that will be cached.
   */
  private static final int SESSION_POOL_SIZE = 20;

  /**
   * Time between reconnection attempts if the first attempt fails (in milliseconds).
   */
  private static final long SLEEP_BETWEEN_CONNECTION_ATTEMPTS = 5000;
  
  /**
   * The JMS connection factory.
   */
  private ConnectionFactory connectionFactory;
  
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
  private Map<ServerUpdateListener, TopicRegistrationDetails> registeredListeners;
  
  /**
   * Listener locks, to prevent concurrent subscription/unsubscription
   * of a given listener.
   */
  private Map<ServerUpdateListener, ReentrantReadWriteLock.WriteLock> listenerLocks;
  
  /**
   * Listeners that need informing about JMS connection and disconnection
   * events.
   */
  private Collection<ConnectionListener> connectionListeners; 
  
  /**
   * Subscribes to the Supervision topic and notifies any registered listeners. 
   */
  private SupervisionListenerWrapper supervisionListenerWrapper;
  
  /**
   * Recording connection status to JMS.
   * Only set in connect and startReconnectThread methods.
   */
  private volatile boolean connected;
  
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
   * Constructor
   */
  public JmsProxyImpl() {
    connectionFactory = new ActiveMQConnectionFactory(System.getProperty("c2mon.jms.broker.url"), 
                                                        System.getProperty("c2mon.jms.broker.user"), 
                                                        System.getProperty("c2mon.jms.broker.passwd"));    
    connected = false;
    shutdownRequested = false;    
    connectingWriteLock = new ReentrantReadWriteLock().writeLock();
    refreshLock = new ReentrantReadWriteLock();
    sessions = new ConcurrentHashMap<MessageListenerWrapper, Session>();    
    topicToWrapper = new ConcurrentHashMap<String, MessageListenerWrapper>();
    registeredListeners = new ConcurrentHashMap<ServerUpdateListener, TopicRegistrationDetails>();
    listenerLocks = new ConcurrentHashMap<ServerUpdateListener, ReentrantReadWriteLock.WriteLock>();
    connectionListeners = new ArrayList<ConnectionListener>(); 
    supervisionListenerWrapper = new SupervisionListenerWrapper();
  }
  
 

  /**
   * Runs until (re)connected. Should only be called by one thread.
   * Use startReconnectThread to run in another thread (only one
   * thread will be started however many calls are made).
   * 
   * <p>Listeners are notified of connection once the connection
   * is reestablished and all topic subscriptions are back.
   */
  private void connect() {
      while (!connected && !shutdownRequested) {
        try {                    
          connection = connectionFactory.createConnection();         
          refreshSubscriptions();
          connected = true;
          connection.setExceptionListener(this);
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
    for (ConnectionListener listener : connectionListeners) {
      listener.onConnection();
    }
  }
  
  /**
   * Notifies all {@link ConnectionListener}s on a disconnection.
   */
  private void notifyConnectionListenerOnDisconnection() {
    for (ConnectionListener listener : connectionListeners) {
      listener.onDisconnection();
    }
  }

  /**
   * Also notifies listeners of disconnection.
   */
  private void disconnect() {
    connected = false;
    notifyConnectionListenerOnDisconnection();
    try {
      connection.close(); //closes all consumers and sessions also
    } catch (JMSException jmsEx) {
      LOGGER.error("Exception caught while attempting to disconnect from JMS - aborting this attempt.", jmsEx);
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
        for (Map.Entry<ServerUpdateListener, TopicRegistrationDetails> entry : registeredListeners.entrySet()) {          
          registerUpdateListener(entry.getKey(), entry.getValue());
        }
        //refresh supervision subscription
        subscribeToSupervisionTopic();
      }      
    } catch (JMSException e) {
      LOGGER.error("Did not manage to refresh Topic subscriptions.", e);
      throw e;
    } finally {
      refreshLock.writeLock().unlock();
    }
    
  }
  
  /**
   * Called when refreshing subscriptions at start up and again
   * if the connection goes down.
   * 
   * @throws JMSException if unable to subsribe
   */
  private void subscribeToSupervisionTopic() throws JMSException {   
    if (connected) {       
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);                   
        Topic topic = session.createTopic(System.getProperty("c2mon.jms.supervision.topic"));
        MessageConsumer consumer = session.createConsumer(topic);                 
        consumer.setMessageListener(supervisionListenerWrapper);        
    } else {           
      throw new JMSException("Not currently connected - will attempt to re-subscribe on reconnection.");     
    }
  }

  @Override
  public boolean isRegisteredListener(final ServerUpdateListener serverUpdateListener) {
    if (serverUpdateListener == null) {
      throw new IllegalArgumentException("isRegisteredListener() method called with null parameter!");
    }
    return registeredListeners.containsKey(serverUpdateListener);
  }

  @Override
  public void registerUpdateListener(final ServerUpdateListener serverUpdateListener, 
                            final TopicRegistrationDetails TopicRegistrationDetails) throws JMSException {   
    refreshLock.readLock().lock();
    try {
      listenerLocks.get(serverUpdateListener).lock();
      try {
        if (!isRegisteredListener(serverUpdateListener)) { //throw exception if ServerUpdateListener null
          try {
            if (connected) {
              String topicName = TopicRegistrationDetails.getTopicName();
              if (topicToWrapper.containsKey(topicName)) {
                topicToWrapper.get(topicName).addListener(serverUpdateListener, TopicRegistrationDetails.getId());
              } else {                
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);               
                Topic topic = session.createTopic(TopicRegistrationDetails.getTopicName());
                MessageConsumer consumer = session.createConsumer(topic);         
                MessageListenerWrapper wrapper = new MessageListenerWrapper(TopicRegistrationDetails.getId(), serverUpdateListener);
                consumer.setMessageListener(wrapper);
                sessions.put(wrapper, session);
              }                             
              registeredListeners.put(serverUpdateListener, TopicRegistrationDetails);
            } else {           
              throw new JMSException("Not currently connected - will attempt to subscribe on reconnection. Attempting to reconnect.");     
            }
          } catch (JMSException e) {
            LOGGER.error("Failed to subscribe to topic - will do so on reconnection.", e);
            registeredListeners.put(serverUpdateListener, TopicRegistrationDetails);
            throw e;
          }
        } else {
          LOGGER.debug("Update listener already registered; skipping registration (for Tag " + TopicRegistrationDetails.getId() + ")");     
        }
      } finally {
        listenerLocks.get(serverUpdateListener).unlock();
      }      
    } finally {
      refreshLock.readLock().unlock();
    }    
  }

  @Override
  public void replaceListener(final ServerUpdateListener registeredListener, final ServerUpdateListener replacementListener) {
    if (registeredListener == null && replacementListener == null) {
      throw new IllegalArgumentException("replaceListener(..) method called with null argument");
    }
    refreshLock.readLock().lock();
    try {
      ReentrantReadWriteLock.WriteLock lock = listenerLocks.get(registeredListener);
      lock.lock();      
      try {
        TopicRegistrationDetails tag = registeredListeners.get(registeredListener); 
        topicToWrapper.get(tag.getTopicName()).addListener(replacementListener, tag.getId());       
        listenerLocks.put(replacementListener, listenerLocks.remove(registeredListener));
        registeredListeners.put(replacementListener, registeredListeners.remove(registeredListener));
      } finally {
        lock.unlock();        
      }
    } finally {
      refreshLock.readLock().unlock();
    }    
  }

  @Override
  public <T extends ClientRequestResult> Collection<T> sendRequest(final JsonRequest<T> jsonRequest, 
                                                   final String queueName, final int timeout) throws JMSException {  
    if (queueName == null) {
      throw new NullPointerException("sendRequest(..) method called with null queue name argument");
    }
    if (connected) {
      Session session = connection.createSession(false, Session.SESSION_TRANSACTED);           
      TextMessage message = session.createTextMessage(jsonRequest.toJson());
      TemporaryQueue replyQueue = session.createTemporaryQueue();
      message.setJMSReplyTo(replyQueue);     
      MessageProducer producer = session.createProducer(new ActiveMQQueue(queueName));
      producer.send(message);
      MessageConsumer consumer = session.createConsumer(replyQueue);
      Message replyMessage = consumer.receive(timeout);
      if (replyMessage == null) {
        LOGGER.error("Null reply received from server on ClientRequest.");
        throw new RuntimeException("Null reply received from server - possible timeout?");
      }      
      return jsonRequest.fromJsonResponse(((TextMessage) replyMessage).getText());      
     
    } else {
      throw new JMSException("Not currently connected: unable to send request at this time.");
    }    
  }

  @Override
  public void unregisterUpdateListener(final ServerUpdateListener serverUpdateListener) {
    refreshLock.readLock().lock();
    try {
      if (isRegisteredListener(serverUpdateListener)) {
        TopicRegistrationDetails subsribedToTag = registeredListeners.get(serverUpdateListener);
        MessageListenerWrapper wrapper = topicToWrapper.get(serverUpdateListener);
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
            registeredListeners.remove(serverUpdateListener);
          }
        }
      }       
    } finally {
      refreshLock.readLock().unlock();
    }       
  }

  @Override
  public void registerConnectionListener(final ConnectionListener connectionListener) {
    if (connectionListener == null) {
      throw new IllegalArgumentException("registerConnectionListener(..) method called with null listener argument");
    }
    connectionListeners.add(connectionListener);
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
    supervisionListenerWrapper.addListener(supervisionListener);           
  }

  @Override
  public void unregisterSupervisionListener(final SupervisionListener supervisionListener) {   
    supervisionListenerWrapper.removeListener(supervisionListener);        
  }

  //Spring lifecycle methods
  
  @Override
  public boolean isAutoStartup() {
    return false;
  }

  @Override
  public void stop(Runnable callback) {
    stop();
    callback.run();
  }

  @Override
  public boolean isRunning() {
    return !shutdownRequested;
  }

  @Override
  public void start() {
    //thread starting connection; is stopped when calling shutdown method
    new Thread(new Runnable() {      
      @Override
      public void run() {
        connect();
      }
    }).start();    
  }
  
  @Override
  public void stop() {
    shutdownRequested = true;
    disconnect();
  }

  @Override
  public int getPhase() {
    return Integer.MAX_VALUE;
  }

}
