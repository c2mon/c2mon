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
package cern.c2mon.client.common.util;

import java.util.IdentityHashMap;
import java.util.Iterator;

/**
 * This is a concurrent and modifiable IdentitySet.<br/>
 * <br/>
 * 
 * <b>This class intentionally violates Set's general contract which uses the
 * equals method when comparing objects.</b> Instead of using
 * {@link Object#equals(Object)} this class checks everything by reference.<br/>
 * <br/>
 * This class supports concurrency, so all methods can be accessed
 * simultaneously. The {@link Iterator} contains the elements which is in the
 * list at the moment the iterator is requested. The {@link Iterator#remove()}
 * is supported.<br/>
 * <br/>
 * 
 * This class extends {@link ConcurrentSet} which is a wrapper around an
 * {@link IdentityHashMap} plus it supports concurrency.
 * 
 * @author vdeila
 * 
 * @param <T>
 *          the type of the elements in the list
 * 
 * @see IdentityHashMap
 * @see ConcurrentSet
 */
public class ConcurrentIdentitySet<T> extends ConcurrentSet<T> {

  /**
   * Constructor
   */
  public ConcurrentIdentitySet() {
    super(new IdentityHashMap<T, Boolean>());
  }

}
