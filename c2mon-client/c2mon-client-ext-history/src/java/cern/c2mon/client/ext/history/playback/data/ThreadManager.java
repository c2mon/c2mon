/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.client.ext.history.playback.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps a count of all running thread.
 * 
 * @author vdeila
 *
 */
public class ThreadManager {

  /** Log4j logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(ThreadManager.class);
  
  /** The list of threads */
  private final List<Thread> threads;

  /** Lock for {@link #threads} */
  private final ReentrantReadWriteLock threadsLock;
  
  /**
   * Constructor
   */
  public ThreadManager() {
    this.threads = new ArrayList<Thread>();
    this.threadsLock = new ReentrantReadWriteLock();
  }
  
  /**
   * Clean up threads that are finish running
   */
  public void clean() {
    final Collection<Thread> threadList = getThreads();
    for (final Thread thread : threadList) {
      if (!thread.isAlive()) {
        removeThread(thread);
      }
    }
  }
  
  /**
   * Creates a thread for the runnable, it is added to the list of threads. It
   * is not started.
   * 
   * @param name
   *          the name of the thread
   * @param runnable
   *          the runnable which will be run
   * @return the created thread
   */
  private Thread create(final String name, final Runnable runnable) {
    final Thread thread = new Thread(runnable, name);
    thread.setDaemon(true);
    addThread(thread);
    return thread;
  }
  
  /**
   * Creates a thread for the runnable, adds it to the list of threads and
   * starts it
   * 
   * @param name
   *          the name of the thread
   * @param runnable
   *          the runnable which will be run
   */
  public void start(final String name, final Runnable runnable) {
    create(name, runnable).start();
  }

  /**
   * Adds the thread to the list of running threads, and starts it.
   * 
   * @param thread
   *          the thread to add and start
   */
  public void start(final Thread thread) {
    addThread(thread);
    thread.start();
  }

  /**
   * @param thread
   *          the thread to add
   */
  private void addThread(final Thread thread) {
    clean();
    threadsLock.writeLock().lock();
    try {
      threads.add(thread);
    }
    finally {
      threadsLock.writeLock().unlock();
    }
  }
  
  /**
   * @param thread
   *          the thread to remove
   */
  private void removeThread(final Thread thread) {
    threadsLock.writeLock().lock();
    try {
      threads.remove(thread);
    }
    finally {
      threadsLock.writeLock().unlock();
    }
  }
  
  /**
   * @return a copy of the list of threads
   */
  private Collection<Thread> getThreads() {
    threadsLock.readLock().lock();
    try {
      return new ArrayList<Thread>(this.threads);
    }
    finally {
      threadsLock.readLock().unlock();
    }
  }
  
  /**
   * Waits until all threads are finish running. If any threads are started
   * while waiting, it will also wait for those.
   */
  public void join() {
    boolean run = true;
    while (run) {
      final Collection<Thread> threadList = getThreads();
      if (threadList.size() > 0) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(String.format("Joining maximum '%s' threads", threadList.size()));
        }
        for (Thread thread : threadList) {
          if (thread.isAlive()) {
            if (LOG.isDebugEnabled()) {
              LOG.debug(String.format("Waiting for thread '%s'", thread.getName()));
            }
            try {
              thread.join();
            }
            catch (InterruptedException e) {
              if (LOG.isDebugEnabled()) {
                LOG.debug(String.format(
                    "The thread '%s' were interrupted", thread.getName()),
                    e);
              }
            }
          }
          removeThread(thread);
        }
      }
      else {
        run = false;
      }
    }
  }
    
}
