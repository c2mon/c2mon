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
