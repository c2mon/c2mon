/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This is a concurrent and modifiable Set.<br/>
 * <br/>
 * This class supports concurrency, so all methods can be accessed
 * simultaneously. The {@link Iterator} contains the elements which is in the
 * list at the moment the iterator is requested. The {@link Iterator#remove()}
 * is supported.<br/>
 * <br/>
 * 
 * This is a wrapper around an {@link IdentityHashMap} plus it supports
 * concurrency.
 * 
 * @author vdeila
 * 
 * @param <T>
 *          the type of the elements in the list
 */
public class ConcurrentSet<T> implements Set<T> {

  /** The actual list where everything is stored */
  private final Map<T, Boolean> list;

  /** The lock for {@link #list} */
  private final ReentrantReadWriteLock listLock;

  /**
   * Constructor
   */
  public ConcurrentSet() {
    this(new HashMap<T, Boolean>());
  }

  /**
   * @param mapImpl
   *          the map implementation to use. {@link HashMap} is the default.
   */
  protected ConcurrentSet(final Map<T, Boolean> mapImpl) {
    this.listLock = new ReentrantReadWriteLock();
    this.list = mapImpl;
  }

  @Override
  public boolean add(final T e) {
    this.listLock.writeLock().lock();
    try {
      list.put(e, Boolean.TRUE);
    }
    finally {
      this.listLock.writeLock().unlock();
    }
    return true;
  }

  @Override
  public boolean addAll(final Collection<? extends T> collection) {
    this.listLock.writeLock().lock();
    try {
      for (T e : collection) {
        list.put(e, Boolean.TRUE);
      }
    }
    finally {
      this.listLock.writeLock().unlock();
    }
    return true;
  }

  @Override
  public void clear() {
    this.listLock.writeLock().lock();
    try {
      list.clear();
    }
    finally {
      this.listLock.writeLock().unlock();
    }
  }

  @Override
  public boolean contains(final Object o) {
    this.listLock.readLock().lock();
    try {
      return list.get(o) != null;
    }
    finally {
      this.listLock.readLock().unlock();
    }
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    this.listLock.readLock().lock();
    try {
      return list.keySet().containsAll(c);
    }
    finally {
      this.listLock.readLock().unlock();
    }
  }

  @Override
  public boolean isEmpty() {
    this.listLock.readLock().lock();
    try {
      return list.isEmpty();
    }
    finally {
      this.listLock.readLock().unlock();
    }
  }

  @Override
  public Iterator<T> iterator() {
    this.listLock.readLock().lock();
    try {
      // Makes a copy of the values, and returns an iterator which supports
      // removing.
      return new ConcurrentIdentitySetIterator<T>(new ArrayList<T>(list.keySet()).iterator());
    }
    finally {
      this.listLock.readLock().unlock();
    }
  }

  @Override
  public boolean remove(final Object o) {
    this.listLock.writeLock().lock();
    try {
      return list.remove(o) != null;
    }
    finally {
      this.listLock.writeLock().unlock();
    }
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    this.listLock.writeLock().lock();
    try {
      return list.keySet().removeAll(c);
    }
    finally {
      this.listLock.writeLock().unlock();
    }
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    this.listLock.writeLock().lock();
    try {
      return list.keySet().retainAll(c);
    }
    finally {
      this.listLock.writeLock().unlock();
    }
  }

  @Override
  public int size() {
    this.listLock.readLock().lock();
    try {
      return list.size();
    }
    finally {
      this.listLock.readLock().unlock();
    }
  }

  @Override
  public Object[] toArray() {
    this.listLock.readLock().lock();
    try {
      return list.keySet().toArray();
    }
    finally {
      this.listLock.readLock().unlock();
    }
  }

  @Override
  public <A> A[] toArray(final A[] a) {
    this.listLock.readLock().lock();
    try {
      return list.keySet().toArray(a);
    }
    finally {
      this.listLock.readLock().unlock();
    }
  }

  /**
   * 
   * @param <E>
   *          the type of the iterator
   */
  private class ConcurrentIdentitySetIterator<E> implements Iterator<E> {
    /** The iterator */
    private final Iterator<E> iterator;

    /** The last object returned by {@link #next()} */
    private E last = null;

    /**
     * @param iterator
     *          the iterator to wrap
     */
    public ConcurrentIdentitySetIterator(final Iterator<E> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return this.iterator.hasNext();
    }

    @Override
    public E next() {
      final E next = this.iterator.next();
      this.last = next;
      return next;
    }

    @Override
    public synchronized void remove() {
      if (this.last != null) {
        ConcurrentSet.this.remove(this.last);
        this.last = null;
      }
      else {
        throw new IllegalStateException("Next must be called first, or remove have already been called for this object.");
      }
    }
  }

}
