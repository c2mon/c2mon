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

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PreDestroy;
import javax.jms.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import cern.c2mon.client.core.jms.ConnectionListener;
import cern.c2mon.client.core.jms.JmsProxy;
import cern.c2mon.client.core.jms.TopicRegistrationDetails;
import cern.c2mon.client.core.listener.TagUpdateListener;

/**
 * This is a helper class of {@link JmsProxyImpl} and takes care of the JMS connection lifecycle.
 * <p>
 * A new session (thread) is created for every Topic subscription. For this
 * reason, the number of different topics across all Tags should be kept to a
 * reasonable number (say around 50) and adjusted upwards if the number of
 * incoming updates is causing performance problems. These sessions are created
 * when needed and closed when possible (i.e. when no more update listeners
 * registered on the topic).
 * <p>
 * Separate sessions are used for listening to supervision events and sending
 * requests to the server (the latter created at request time).
 * <p>
 * Connect and disconnect methods are synchronized to prevent them running in
 * parallel.
 * 
 * @author Matthias Braeger
 */
@Component
@Slf4j
class JmsConnectionHandler {
  
  /**
   * Buffer before warnings for slow consumers are sent, for tags, alarms (where
   * larger buffer is desirable).
   */
  private static final int HIGH_LISTENER_QUEUE_SIZE = 10000;
  
  /**
  * Time between reconnection attempts if the first attempt fails (in
  * milliseconds).
  */
 private static final long SLEEP_BETWEEN_CONNECTION_ATTEMPTS = 5000;

  /**
   * The unique JMS connection used.
   */
  @Getter
  private ActiveMQConnection connection;
  
  /**
   * The JMS connection factory.
   */
  private final ConnectionFactory jmsConnectionFactory;
  
  /**
   * No refresh of the subscriptions is allowed at the same time as
   * registrations or unregistrations, since a refresh involves cleaning all the
   * internal maps and re-subscribing consumers on the various sessions.
   */
  private final ReentrantReadWriteLock refreshLock = new ReentrantReadWriteLock();
  
  /**
   * Listener exclusive lock, to prevent concurrent subscription/unsubscription of listeners.
   */
  private final ReentrantReadWriteLock.WriteLock listenerLock = new ReentrantReadWriteLock().writeLock();
  
  /**
   * Listeners that need informing about JMS connection and disconnection
   * events.
   */
  private final Collection<ConnectionListener> connectionListeners = new ArrayList<>();
  
  private final ReentrantReadWriteLock connectionListenersLock = new ReentrantReadWriteLock();
  
  /**
   * A lock used to prevent multiple reconnection threads being started.
   */
  private final ReentrantReadWriteLock.WriteLock connectingWriteLock = new ReentrantReadWriteLock().writeLock();
  
  /**
   * Notified on slow consumer detection.
   */
  private final SlowConsumerListener slowConsumerListener;
  
  /**
   * Recording connection status to JMS. Only set in connect and
   * startReconnectThread methods.
   */
  @Getter
  private volatile boolean connected;

  /**
   * Track start/stop status.
   */
  private volatile boolean running;

  /**
   * A final shutdown of this JmsProxy has been requested.
   */
  @Getter
  private volatile boolean shutdownRequested;
  
  /**
   * 1-1 correspondence between ServerUpdateListeners and the Tags they are
   * receiving updates for (usually both are the same object).
   */
  private final Map<TagUpdateListener, TopicRegistrationDetails> registeredListeners = new ConcurrentHashMap<>();
  
  /**
   * Indicates which {@link MessageListenerWrapper} is listening to a given
   * topic (each wrapper listens to a single topic).
   * <p>
   * Each wrapper has its own internal thread that needs stop/starting when the
   * wrapper is removed or created.
   */
  @Getter
  private final Map<String, MessageListenerWrapper> topicToWrapper = new ConcurrentHashMap<>();
  
  /**
   * Points to the JMS Session started for a given wrapper. Only the sessions
   * used for Tag update subscriptions are referenced here.
   */
  private Map<MessageListenerWrapper, Session> sessions = new ConcurrentHashMap<>();
  
  /**
   * Threads used for polling topic queues.
   */
  private final ExecutorService topicPollingExecutor;
  
  @Setter
  private JmsSubscriptionHandler jmsSubscriptionHandler;
  
  @Autowired
  public JmsConnectionHandler(@Qualifier("clientJmsConnectionFactory") final ConnectionFactory connectionFactory,
      final SlowConsumerListener slowConsumerListener,
      @Qualifier("topicPollingExecutor") final ExecutorService topicPollingExecutor) {
    this.topicPollingExecutor = topicPollingExecutor;
    this.jmsConnectionFactory = connectionFactory;
    this.slowConsumerListener = slowConsumerListener;

    connected = false;
    shutdownRequested = false;
  }

  /**
   * Refreshes all the Topic subscriptions. Assumes previous connection/sessions
   * are now inactive.
   *
   * @throws JMSException
   *           if problem subscribing
   */
  private void refreshSubscriptions() throws JMSException {
    refreshLock.writeLock().lock();
    try {
      if (!registeredListeners.isEmpty()) {
        sessions.clear();
        topicToWrapper.clear();
        // refresh all registered listeners for Tag updates
        for (Map.Entry<TagUpdateListener, TopicRegistrationDetails> entry : registeredListeners.entrySet()) {
          registerUpdateListener(entry.getKey(), entry.getValue());
        }
      }
      jmsSubscriptionHandler.refreshAllSubscriptions(connection);
    } catch (JMSException e) {
      log.error("Did not manage to refresh Topic subscriptions", e);
      throw e;
    } finally {
      refreshLock.writeLock().unlock();
    }
  }

  /**
   * Runs until (re)connected. Should only be called by one thread. Use
   * startReconnectThread to run in another thread (only one thread will be
   * started however many calls are made).
   * <p>
   * Listeners are notified of connection once the connection is reestablished
   * and all topic subscriptions are back.
   */
  private synchronized void connect() {
    while (!connected && !shutdownRequested) {
      try {
        connection = (ActiveMQConnection) jmsConnectionFactory.createConnection();
        connection.start();
        connection.addTransportListener((ActiveMQTransportListener) this::startReconnectThread);
        refreshSubscriptions();
        connected = true;
      } catch (Exception e) {
        log.error("Exception caught while trying to refresh the JMS connection; sleeping 5s before retrying.", e);
        try {
          wait(SLEEP_BETWEEN_CONNECTION_ATTEMPTS);
        } catch (InterruptedException interEx) {
          log.error("InterruptedException caught while waiting to reconnect.", interEx);
        }
      }
    }
    if (connected) {
      notifyConnectionListenerOnConnection();
    }
  }

  protected void ensureConnection() {
    if (!running) {
      init();
    }
  }
  
  private void init() {
    running = true;
    shutdownRequested = false;
    setActiveMQConnectionPrefix();
    connect();
  }
  
  /**
   * Sets the prefix id on the JMS connection factory. Only works if JMS
   * connection factory is ActiveMQ.
   */
  private void setActiveMQConnectionPrefix() {
    String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

    String hostname = null;
    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      log.info("Couldn't get hostname", e);
    }

    String clientIdPrefix = "C2MON-CLIENT-" + System.getProperty("user.name") + "@" + hostname + "[" + pid + "]";

    if (jmsConnectionFactory instanceof ActiveMQConnectionFactory) {
      ((ActiveMQConnectionFactory) jmsConnectionFactory).setClientIDPrefix(clientIdPrefix);
    }
  }

  /**
   * @see JmsProxy#registerUpdateListener(TagUpdateListener, TopicRegistrationDetails)
   */
  protected void registerUpdateListener(final TagUpdateListener serverUpdateListener, final TopicRegistrationDetails topicRegistrationDetails) throws JMSException {
    if (topicRegistrationDetails == null) {
      throw new NullPointerException("Trying to register a TagUpdateListener with null RegistrationDetails!");
    }

    if (serverUpdateListener == null) {
      throw new NullPointerException("TagUpdateListener must not be null!");
    }

    ensureConnection();

    refreshLock.readLock().lock();
    try {
      listenerLock.lock();
      try {
        boolean refreshSubscriptions = refreshLock.isWriteLocked();

        if (refreshSubscriptions || !isRegisteredListener(serverUpdateListener)) { 
          // throw exception if TagUpdateListener null
          try {
            if (refreshSubscriptions || connected) {
              String topicName = topicRegistrationDetails.getTopicName();
              if (topicToWrapper.containsKey(topicName)) {
                topicToWrapper.get(topicName).addListener(serverUpdateListener, topicRegistrationDetails.getId());
              } else {
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Topic topic = session.createTopic(topicRegistrationDetails.getTopicName());
                MessageConsumer consumer = session.createConsumer(topic);
                MessageListenerWrapper wrapper = new MessageListenerWrapper(topicRegistrationDetails.getId(), serverUpdateListener, HIGH_LISTENER_QUEUE_SIZE,
                    slowConsumerListener, topicPollingExecutor);
                wrapper.start();
                consumer.setMessageListener(wrapper);
                topicToWrapper.put(topicName, wrapper);
                sessions.put(wrapper, session);
              }

              if (!refreshSubscriptions) {
                registeredListeners.put(serverUpdateListener, topicRegistrationDetails);
              }
            } else {
              throw new JMSException("Not currently connected - will attempt to subscribe on reconnection. Attempting to reconnect.");
            }
          } catch (JMSException e) {
            log.error("Failed to subscribe to topic - will do so on reconnection.", e);
            if (!refreshSubscriptions) {
              registeredListeners.put(serverUpdateListener, topicRegistrationDetails);
            }
            throw e;
          }
        } else {
          log.debug("Update listener already registered; skipping registration (for Tag {})", topicRegistrationDetails.getId());
        }
      } finally {
        listenerLock.unlock();
      }
    } finally {
      refreshLock.readLock().unlock();
    }
  }
  
  protected void unregisterUpdateListener(final TagUpdateListener serverUpdateListener) {
    refreshLock.readLock().lock();
    try {
      listenerLock.lock();
      try {
        if (isRegisteredListener(serverUpdateListener)) {
          TopicRegistrationDetails subsribedToTag = registeredListeners.get(serverUpdateListener);
          MessageListenerWrapper wrapper = topicToWrapper.get(subsribedToTag.getTopicName());
          wrapper.removeListener(subsribedToTag.getId());
          if (wrapper.isEmpty()) {
            // no subscribed listeners, so close session
            log.trace("No listeners registered to topic {} so closing down MessageListenerWrapper", subsribedToTag.getTopicName());
            try {
              Session session = sessions.get(wrapper);
              session.close();
            } catch (JMSException ex) {
              log.error("Failed to unregister properly from a Tag update; subscriptions will be refreshed.");
              startReconnectThread();
            } finally {
              wrapper.stop();
              sessions.remove(wrapper);
              topicToWrapper.remove(subsribedToTag.getTopicName());
            }
          }
          registeredListeners.remove(serverUpdateListener);
        }
      } finally {
        listenerLock.unlock();
      }
    } finally {
      refreshLock.readLock().unlock();
    }
  }
  
  boolean isRegisteredListener(final TagUpdateListener serverUpdateListener) {
    if (serverUpdateListener == null) {
      throw new NullPointerException("isRegisteredListener() method called with null parameter!");
    }
    return registeredListeners.containsKey(serverUpdateListener);
  }
  
  protected void replaceListener(final TagUpdateListener registeredListener, final TagUpdateListener replacementListener) {
    if (registeredListener == null && replacementListener == null) {
      throw new NullPointerException("replaceListener(..) method called with null argument");
    }
    refreshLock.readLock().lock();
    try {
      listenerLock.lock();
      try {
        TopicRegistrationDetails tag = registeredListeners.get(registeredListener);
        topicToWrapper.get(tag.getTopicName()).addListener(replacementListener, tag.getId());
        registeredListeners.put(replacementListener, registeredListeners.remove(registeredListener));
      } finally {
        listenerLock.unlock();
      }
    } finally {
      refreshLock.readLock().unlock();
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
   * Only starts the thread if it is not already started (thread will always be
   * started if connected has been set to false).
   */
  protected void startReconnectThread() {
    // lock to prevent multiple threads from starting (before connected flag is
    // modified)
    connectingWriteLock.lock();
    try {
      if (connected) {
        // notifies listeners
        disconnect();
        new Thread(() -> connect()).start();
      }
    } finally {
      connectingWriteLock.unlock();
    }
  }
  
  /**
   * Disconnects and notifies listeners of disconnection.
   */
  private synchronized void disconnect() {
    disconnectQuietly();
    notifyConnectionListenerOnDisconnection();
  }

  /**
   * Stops all topic listeners and disconnects; connection listeners are not
   * notified. Used at shutdown.
   */
  private synchronized void disconnectQuietly() {
    connected = false;
    // these listeners are re-created
    for (Map.Entry<String, MessageListenerWrapper> entry : topicToWrapper.entrySet()) {
      entry.getValue().stop();
    }
    if (connection != null && !connection.isClosed() && connection.isTransportFailed()) {
      try {
        connection.close(); // closes all consumers and sessions also
      } catch (JMSException e) {
        String message = "Exception caught while attempting to disconnect from JMS: " + e.getMessage();
        log.error(message);
        log.debug(message, e);
      }
    }
  }
  
  protected void registerConnectionListener(final ConnectionListener connectionListener) {
    if (connectionListener == null) {
      throw new NullPointerException("registerConnectionListener(..) method called with null listener argument");
    }

    ensureConnection();

    connectionListenersLock.writeLock().lock();
    try {
      connectionListeners.add(connectionListener);
      if (connected) {
        connectionListener.onConnection();
      } else {
        connectionListener.onDisconnection();
      }
    } finally {
      connectionListenersLock.writeLock().unlock();
    }
  }
  
  /**
   * Shuts down the connection to JMS.
   */
  @PreDestroy
  public void stop() {
    log.debug("Stopping JmsProxy and dependent listeners");
    shutdownRequested = true;
    topicPollingExecutor.shutdown();
    disconnectQuietly();
    connectionListenersLock.writeLock().lock();
    try {
      connectionListeners.clear();
    } finally {
      connectionListenersLock.writeLock().unlock();
    }
    running = false;
  }
}
