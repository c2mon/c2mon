package cern.c2mon.client.jms.impl;

import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cern.c2mon.client.jms.ClientHealthListener;
import cern.c2mon.client.jms.ClientHealthMonitor;

@Service
public class ClientHealthMonitorImpl implements ClientHealthMonitor, SlowConsumerListener {

  private static final Logger LOGGER = Logger.getLogger("CentralLogger");
  
  /**
   * Listeners.
   */
  private HashSet<ClientHealthListener> listeners  = new HashSet<ClientHealthListener>();
  
  /**
   * Lock to access listener set.
   */
  private ReentrantReadWriteLock listenerLock = new ReentrantReadWriteLock();
  
  /**
   * Will only notify listeners once.
   */
  private volatile boolean slowConsumerNotified = false;
  
  @Override
  public void addHealthListener(final ClientHealthListener clientHealthListener) {
    listenerLock.writeLock().lock();
    try {
      listeners.add(clientHealthListener);
    } finally {
      listenerLock.writeLock().unlock();     
    }    
  }
  
  @Override
  public void removeHealthListener(final ClientHealthListener clientHealthListener) {
    listenerLock.writeLock().lock();
    try {
      listeners.remove(clientHealthListener);
    } finally {
      listenerLock.writeLock().unlock();     
    }    
  }

  @Override
  public void onSlowConsumer(String details) {
    LOGGER.warn("Slow update consumer detected: " + details);
    if (!slowConsumerNotified) { 
      listenerLock.writeLock().lock();
      try {
        for (ClientHealthListener listener : listeners) {
          listener.onSlowUpdateListener(details);
        }
        slowConsumerNotified = true;
      } finally {
        listenerLock.writeLock().unlock();     
      }
    }
  }

}
