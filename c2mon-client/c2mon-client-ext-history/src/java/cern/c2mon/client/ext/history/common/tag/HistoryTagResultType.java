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
package cern.c2mon.client.ext.history.common.tag;

import java.sql.Timestamp;


/**
 * The types of data that can be returned from a {@link HistoryTagImpl}. Is the type
 * of one of the parameters in {@link HistoryTagConfiguration}, declared in the
 * {@link HistoryTagParameter} enum.
 * 
 * @author vdeila
 */
public enum HistoryTagResultType {

  /** The actual values */
  Values(Object[].class),
  /** The labels */
  Labels(Timestamp[].class),
  /** The minimum x value */
  XMin(Double.class),
  /** The maximum x value */
  XMax(Double.class),
  /** The minimum y value */
  YMin(Double.class),
  /** The maximum y value */
  YMax(Double.class),
  /**
   * The result is only defined by {@link HistoryTagParameter#LoadingValue},
   * {@link HistoryTagParameter#FailedValue} and
   * {@link HistoryTagParameter#ActiveValue}
   */
  Conditional(Object.class);
  
  /** The class that this type will return */
  private final Class< ? > resultClass;
  
  /**
   * @param resultClass the type which this type will return ({@link HistoryTagImpl#getValue()})
   */
  private HistoryTagResultType(final Class< ? > resultClass) {
    this.resultClass = resultClass;
  }
  
  /**
   * @param lookup
   *          the name of the value to look up, not case sensitive
   * @return the history type that were requested, never <code>null</code>
   * @throws IllegalArgumentException
   *           if the <code>lookup</code> parameter isn't a valid
   *           {@link HistoryTagResultType}
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
  public Class< ? > getResultClass() {
    return resultClass;
  }
  
}
