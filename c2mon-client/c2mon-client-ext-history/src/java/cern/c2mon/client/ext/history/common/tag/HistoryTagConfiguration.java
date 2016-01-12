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

/**
 * This interface describes the methods for a configuration for a
 * {@link HistoryTag}. It containt methods for creating an expression of the
 * configuration.
 * 
 * @see #createExpression()
 * @see HistoryTagExpressionInterpreter
 * @see HistoryTagImpl
 * 
 * @author vdeila
 */
public interface HistoryTagConfiguration {

  /**
   * @return <code>true</code> if all needed parameters are set,
   *         <code>false</code> otherwise
   */
  boolean validate();

  /**
   * @param parameter
   *          the parameter to get the value of
   * @return the value of the parameter.
   */
  Object getValue(final HistoryTagParameter parameter);

  /**
   * @param <T>
   *          the type of object that will be returned
   * @param parameter
   *          the parameter to get the value for
   * @param clazz
   *          the class for the parameter, should be
   *          {@link HistoryTagParameter#getType()}.
   * @return the value for the given <code>parameter</code>
   * @throws ClassCastException
   *           if the <code>clazz</code> is wrong, that is if
   *           {@link HistoryTagParameter#getType()} is not assignable to
   *           <code>clazz</code>
   */
  <T> T getValue(final HistoryTagParameter parameter, final Class<T> clazz);

  /**
   * @param parameter
   *          the parameter to get the value of
   * @param value
   *          the value to set for the parameter.
   * @throws ClassCastException
   *           if the <code>value</code> is of the wrong type
   */
  void setValue(final HistoryTagParameter parameter, final Object value);

  /**
   * Creates an expression of the current configurations. This expression can be
   * interpreted by {@link HistoryTagExpressionInterpreter}
   * 
   * @return an expression representing this object
   * @throws HistoryTagExpressionException
   *           if one or more required values are missing.
   */
  String createExpression() throws HistoryTagExpressionException;

  /**
   * @return the tag id
   */
  Long getTagId();

  /**
   * @return <code>true</code> if the initial records will be included
   */
  Boolean isInitialRecord();

  /**
   * @return <code>true</code> if the supervision events will be included
   */
  Boolean isSupervision();

  /**
   * @return The maximum number of records that will be retrieved
   */
  Integer getRecords();

  /**
   * @return the type of result that is requested
   */
  HistoryTagResultType getResultType();

  /**
   * @return converts {@link HistoryTagParameter#Days} and
   *         {@link HistoryTagParameter#Hours} into milliseconds,
   *         <code>null</code> if none of them are defined.
   */
  Long getTotalMilliseconds();

}
