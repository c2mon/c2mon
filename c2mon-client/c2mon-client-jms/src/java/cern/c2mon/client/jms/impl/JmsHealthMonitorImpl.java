package cern.c2mon.client.jms.impl;

import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Service;

import cern.c2mon.client.jms.JmsHealthListener;
import cern.c2mon.client.jms.JmsHealthMonitor;

@Service
public class JmsHealthMonitorImpl implements JmsHealthMonitor, SlowConsumerListener {

  /**
   * Listeners.
   */
  private HashSet<JmsHealthListener> listeners  = new HashSet<JmsHealthListener>();
  
  /**
   * Lock to access listener set.
   */
  private ReentrantReadWriteLock listenerLock = new ReentrantReadWriteLock();
  
  /**
   * Will only notify listeners once.
   */
  private volatile boolean slowConsumerNotified = false;
  
  @Override
  public void addHealthListener(final JmsHealthListener jmsHealthListener) {
    listenerLock.writeLock().lock();
    try {
      listeners.add(jmsHealthListener);
    } finally {
      listenerLock.writeLock().unlock();     
    }    
  }
  
  @Override
  public void removeHealthListener(final JmsHealthListener jmsHealthListener) {
    listenerLock.writeLock().lock();
    try {
      listeners.remove(jmsHealthListener);
    } finally {
      listenerLock.writeLock().unlock();     
    }    
  }

  @Override
  public void onSlowConsumer(String details) {    
    if (!slowConsumerNotified) { 
      listenerLock.writeLock().lock();
      try {
        for (JmsHealthListener listener : listeners) {
          listener.slowConsumerDetected(details);
        }
        slowConsumerNotified = true;
      } finally {
        listenerLock.writeLock().unlock();     
      }
    }
  }

}
