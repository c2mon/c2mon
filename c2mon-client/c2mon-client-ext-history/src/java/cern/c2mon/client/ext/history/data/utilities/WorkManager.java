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
package cern.c2mon.client.ext.history.data.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is used to keep track of elements that are currently being worked
 * on. This class supports multithreading as all calls are thread safe.
 * 
 * Example of the logic: 
 * 1. You add a list of elements that you want to
 * start to work on. All the elements in the parameter is returned
 * 2. You add another list of elements that you want to start
 * to work on, but returns only the tags which is not already being worked on.
 * 3. You unregister the tags you are finish working on.
 * 
 * @author vdeila
 * 
 * @param <T>
 *          The type of elements you would like to add
 */
public class WorkManager<T> {
  /**
   * The list of elements that is currently being processed
   */
  private final List<T> currentlyProcessing = new ArrayList<T>();

  /**
   * The lock to access <code>currentlyProcessing</code>
   */
  private final ReentrantReadWriteLock currentlyProcessingLock = new ReentrantReadWriteLock();

  /**
   * 
   * @param elements
   *          The elements to unregister work on
   */
  public void unregisterWork(final Collection<T> elements) {
    try {
      this.currentlyProcessingLock.writeLock().lock();
      this.currentlyProcessing.removeAll(elements);
    }
    finally {
      this.currentlyProcessingLock.writeLock().unlock();
    }
  }

  /**
   * 
   * @param elements
   *          The elements you want to register
   * @return A collection of the registered elements. If the <code>elements</code>
   *         parameter contains an element which was not returned it means that
   *         this element already is being worked on.
   */
  public Collection<T> registerWork(final Collection<T> elements) {
    // Keeps a list of elements that are registered
    final List<T> registeredElements = new ArrayList<T>();
    try {
      this.currentlyProcessingLock.writeLock().lock();

      // Iterates through the list to see which is already being worked on
      for (T element : elements) {
        if (!this.currentlyProcessing.contains(element)) {
          if (this.currentlyProcessing.add(element)) {
            registeredElements.add(element);
          }
        }
      }
    }
    finally {
      this.currentlyProcessingLock.writeLock().unlock();
    }
    return registeredElements;
  }
}
