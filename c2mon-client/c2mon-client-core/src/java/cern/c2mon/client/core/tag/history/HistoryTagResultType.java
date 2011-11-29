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
package cern.c2mon.client.core.tag.history;

import java.sql.Timestamp;

import cern.c2mon.client.core.tag.HistoryTag;

/**
 * The types of data that can be returned from a {@link HistoryTag}. Is the type
 * of one of the parameters in {@link HistoryTagConfiguration}, declared in the
 * {@link HistoryTagParameter} enum.
 * 
 * @author vdeila
 */
public enum HistoryTagResultType {

  /** The actual values */
  Values(Object[].class),
  /** The labels */
  Labels(Timestamp[].class);
  
  /** The class that this type will return */
  private final Class< ? > resultClass;
  
  /**
   * @param resultClass the type which this type will return ({@link HistoryTag#getValue()})
   */
  private HistoryTagResultType(final Class< ? > resultClass) {
    this.resultClass = resultClass;
  }
  
  /**
   * @param lookup the name of the value to look up, case insensitive
   * @return the history type that were requested, never <code>null</code>
   * @throws IllegalArgumentException if <code>lookup</code> isn't a valid {@link HistoryTagResultType}
   */
  public static HistoryTagResultType lookup(final String lookup) throws IllegalArgumentException {
    try {
      return valueOf(lookup);
    }
    catch (IllegalArgumentException e) { }
    for (HistoryTagResultType value : values()){
      if (value.toString().compareToIgnoreCase(lookup) == 0) {
        return value;
      }
    }
    throw new IllegalArgumentException(String.format("'%s' is not a valid '%s'", lookup, HistoryTagResultType.class.getSimpleName()));
  }

  /**
   * @return the class that this type will return
   */
  public Class<?> getResultClass() {
    return resultClass;
  }
  
}
