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
package cern.c2mon.shared.util.buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Extends the {@link LinkedBlockingQueue} by a new {@link #poll(long, int)}
 * method that allows to extract a list of elements from the queue.
 *  
 * @author Matthias Braeger
 *
 * @param <E> the type of elements held in this collection 
 */
public class SynchroBufferQueue<E> extends LinkedBlockingQueue<E> {
  
  /** Serial version UID */
  private static final long serialVersionUID = 8256652758201687760L;

  /**
   * Retrieves and removes up to <code>maxSize</code> elements from the head of
   * this queue, waiting up to the specified wait time if necessary for all elements
   * to become available.
   * 
   * @param timeout how long to wait in milliseconds before giving up. Passing a timeout <= 0
   *                will immediately return the call with an empty list.
   * @param maxSize The maximum elements that shall be put into the list before interrupting the poll.
   *                 This only applies as long as the given <code>timeout</code> is not reached.
   *                 Passing a maxSize = 0 will immediately return the call with an empty list. A
   *                 negative maxSize will result in an {@link IllegalArgumentException}
   * @return A list of elements which will have maximum <code>maxCount</code> elements
   * @throws InterruptedException if interrupted while waiting
   */
  public List<E> poll(long timeout, int maxSize) throws InterruptedException {
    long startTime = System.currentTimeMillis();
    List<E> list = new ArrayList<>(maxSize);
    E element;
    
    long nextTimeout =  getNextTimeout(startTime, timeout);
    while (nextTimeout > 0 && list.size() < maxSize) {
      element = super.poll(nextTimeout, TimeUnit.MILLISECONDS);
      if (element != null) {
        list.add(element);
      }
      nextTimeout =  getNextTimeout(startTime, timeout);
    }
    
    return list;
  }
  
  /**
   * Used to 
   * @param startTime time when the poll was started
   * @param timeout The maximum time in milliseconds that the poll should block
   * @return
   */
  private long getNextTimeout(long startTime, long timeout) {
    return startTime + timeout - System.currentTimeMillis();
  }
  
}
