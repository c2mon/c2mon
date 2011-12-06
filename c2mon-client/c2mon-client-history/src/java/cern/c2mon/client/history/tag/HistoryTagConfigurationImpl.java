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
package cern.c2mon.client.history.tag;

import java.util.HashMap;
import java.util.Map;

import cern.c2mon.client.common.history.tag.HistoryTag;
import cern.c2mon.client.common.history.tag.HistoryTagConfiguration;
import cern.c2mon.client.common.history.tag.HistoryTagExpressionException;
import cern.c2mon.client.common.history.tag.HistoryTagParameter;
import cern.c2mon.client.common.history.tag.HistoryTagResultType;
import cern.tim.shared.common.type.TypeConverter;

/**
 * This is a configuration for a {@link HistoryTagImpl}. Can be used to create an
 * expression for a history tag.
 * 
 * @see #createExpression()
 * @see HistoryTagExpressionInterpreter
 * @see HistoryTag
 * 
 * @author vdeila
 */
public class HistoryTagConfigurationImpl implements HistoryTagConfiguration {
  
  /** The values */
  private final Map<HistoryTagParameter, Object> values;
  
  /**
   * Creates an instance with default values (Mostly <code>null</code> values)
   */
  public HistoryTagConfigurationImpl() {
    this.values = new HashMap<HistoryTagParameter, Object>();
  }
  
  /**
   * @param expression
   *          an expression created with the
   *          {@link HistoryTagConfiguration#createExpression()}
   * @return the configurations specified by the <code>expression</code>.
   * @throws HistoryTagExpressionException
   *           if any of the arguments is invalid
   */
  public static HistoryTagConfiguration valueOf(final String expression) throws HistoryTagExpressionException {
    return new HistoryTagExpressionInterpreter(expression).getHistoryTagConfiguration();
  }
  
  /**
   * @return <code>true</code> if all needed parameters are set,
   *         <code>false</code> otherwise
   */
  public boolean validate() {
    try {
      validateParameters();
      return true;
    }
    catch (HistoryTagExpressionException e) {
      return false;
    }
  }
  
  /**
   * @param parameter the parameter to get the value of 
   * @return the value of the parameter.
   */
  public Object getValue(final HistoryTagParameter parameter) {
    final Object value = this.values.get(parameter);
    if (value != null) {
      return value;
    }
    return parameter.getDefaultValue(this);
  }
  
  /**
   * @param <T> the type of object that will be returned
   * @param parameter the parameter to get the value for
   * @param clazz the class for the parameter, should be {@link HistoryTagParameter#getType()}.
   * @return the value for the given <code>parameter</code>
   * @throws ClassCastException if the clazz is wrong.
   */
  public <T> T getValue(final HistoryTagParameter parameter, final Class<T> clazz) {
    if (parameter.getType() == clazz) {
      final Object value = getValue(parameter);
      if (value == null || clazz.isInstance(value)) {
        return clazz.cast(value);
      }
      else {
        throw new RuntimeException(String.format("The value is of class '%s', but a %s was expected for the %s '%s'.", 
            value.getClass().getSimpleName(), 
            parameter.getClass().getSimpleName(), 
            HistoryTagParameter.class.getSimpleName(),
            parameter.toString()));
      }
    }
    else {
      throw new ClassCastException(String.format("The %s '%s' is not of class '%s'. An '%s' is expected as 'clazz' parameter.", 
          HistoryTagParameter.class.getSimpleName(), parameter.toString(), clazz.getSimpleName(), parameter.getClass().getSimpleName()));
    }
  }
  
  /**
   * @param parameter
   *          the parameter to get the value of
   * @param value
   *          the value to set for the parameter.
   * @throws ClassCastException
   *           if the <code>value</code> is of the wrong type
   */
  public void setValue(final HistoryTagParameter parameter, final Object value) {
    Object castedValue = value;
    if (value != null && !parameter.getType().isInstance(value)) {
      try {
        castedValue = TypeConverter.castToType(value, parameter.getType());
      }
      catch (Exception e) {
        throw new ClassCastException(String.format("The value of type '%s' cannot be set for the %s '%s' because it expects a value of type '%s'", 
            value.getClass().getName(), 
            HistoryTagParameter.class.getSimpleName(), 
            parameter.toString(), 
            parameter.getType().getName()));
      }
    }
    if (castedValue != null) {
      this.values.put(parameter, castedValue);
    }
    else {
      this.values.remove(parameter);
    }
  }
  
  /**
   * Validates that all the minimum required set of parameters has a value.
   * 
   * @throws HistoryTagExpressionException
   *           if one or more required values are missing.
   */
  private void validateParameters() throws HistoryTagExpressionException {
    if (getValue(HistoryTagParameter.Result) == null) {
      throw new HistoryTagExpressionException("A result type must be set!");
    }
    if (getValue(HistoryTagParameter.TagId) == null) {
      throw new HistoryTagExpressionException(String.format(
          "The '%s' cannot be null",
          HistoryTagParameter.TagId.getArgument()));
    }
    if (getValue(HistoryTagParameter.Records) == null 
        && getValue(HistoryTagParameter.Days) == null) {
      throw new HistoryTagExpressionException(
          String.format("Either '%s' or '%s' must be set!",
              HistoryTagParameter.Records.getArgument(),
              HistoryTagParameter.Days.getArgument()
              ));
    }
  }
  
  /**
   * Creates an expression of the current configurations. This expression can be
   * interpreted by {@link HistoryTagExpressionInterpreter}
   * 
   * @return an expression representing this object
   * @throws HistoryTagExpressionException
   *           if one or more required values are missing.
   */
  public String createExpression() throws HistoryTagExpressionException {
    validateParameters();
    
    final StringBuilder expr = new StringBuilder();
    
    for (HistoryTagParameter parameter : HistoryTagParameter.values()) {
      final Object value = getValue(parameter);
      if (value != null) {
        if (parameter.getArgument() != null) {
          expr.append(parameter.getArgument());
          expr.append("=");
        }
        String valueStr = value.toString();
        if (valueStr.indexOf(' ') != -1) {
          if (valueStr.indexOf('\'') != -1) {
            throw new HistoryTagExpressionException(String.format(
                "Single qoutes ' is not allowed. The argument \"%s\" with the value \"%s\" cannot be set.",
                parameter.getArgument(), valueStr));
          }
          valueStr = String.format("'%s'", valueStr);
        }
        expr.append(valueStr);
        expr.append(' ');
      }
    }
    
    return expr.toString().trim();
  }

  /**
   * Compares all objects that have
   * {@link HistoryTagParameter#isAffectingQuery()} set to <code>true</code>
   * 
   * @param obj
   *          the object to compare with
   * @return <code>true</code> if the <code>obj</code> equals to this object,
   *         only comparing the parameters that affects the query to the
   *         database
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof HistoryTagConfigurationImpl))
      return false;
    final HistoryTagConfigurationImpl other = (HistoryTagConfigurationImpl) obj;
    for (HistoryTagParameter parameter : HistoryTagParameter.values()) {
      if (useParameterInCompare(parameter)) {
        final Object myValue = this.getValue(parameter);
        final Object otherValue = other.getValue(parameter);
        if (myValue != otherValue
            && myValue != null
            && !myValue.equals(otherValue)) {
          return false;
        }
      }
    }
    
    final Object myValue = this.getTotalMilliseconds();
    final Object otherValue = other.getTotalMilliseconds();
    if (myValue != otherValue
        && myValue != null
        && !myValue.equals(otherValue)) {
      return false;
    }
    return true;
  };
  
  /**
   * @return the hash code, only including the parameters that affects the query
   *         to the database
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    for (HistoryTagParameter parameter : HistoryTagParameter.values()) {
      if (useParameterInCompare(parameter)) {
        final Object value = this.getValue(parameter);
        result = prime * result + ((value == null) ? 0 : value.hashCode());
      }
    }
    final Long time = getTotalMilliseconds();
    result = prime * result + ((time == null) ? 0 : time.hashCode());
    return result;
  }
  
  /**
   * This function is used by {@link #equals(Object)} and {@link #hashCode()} to
   * determine if the parameter affects the result or not.
   * 
   * @param parameter
   *          the parameter in question
   * @return <code>true</code> if the parameter should be used in affecting the
   *         result.
   */
  private boolean useParameterInCompare(final HistoryTagParameter parameter) {
    // Do only check the parameters which changes the query to the database.
    // The days and hours is added together, and is checked separately.
    return parameter.isAffectingQuery()
        && parameter != HistoryTagParameter.Days
        && parameter != HistoryTagParameter.Hours;
  }

  /**
   * @return the tag id
   */
  public Long getTagId() {
    return getValue(HistoryTagParameter.TagId, Long.class);
  }

  /**
   * @return <code>true</code> if the initial records will be included
   */
  public Boolean isInitialRecord() {
    return getValue(HistoryTagParameter.InitialRecord, Boolean.class);
  }

  /**
   * @return <code>true</code> if the supervision events will be included
   */
  public Boolean isSupervision() {
    return getValue(HistoryTagParameter.Supervision, Boolean.class);
  }

  /**
   * @return The maximum number of records that will be retrieved
   */
  public Integer getRecords() {
    return getValue(HistoryTagParameter.Records, Integer.class);
  }
  
  /**
   * @return the type of result that is requested
   */
  public HistoryTagResultType getResultType() {
    return getValue(HistoryTagParameter.Result, HistoryTagResultType.class);
  }
  
  /**
   * @return converts {@link HistoryTagParameter#Days} and
   *         {@link HistoryTagParameter#Hours} into milliseconds,
   *         <code>null</code> if none of them are defined.
   */
  public Long getTotalMilliseconds() {
    Double days = getValue(HistoryTagParameter.Days, Double.class);
    Double hours = getValue(HistoryTagParameter.Hours, Double.class);
    if (days == null && hours == null) {
      return null;
    }
    if (days == null) {
      days = 0.0;
    }
    if (hours == null) {
      hours = 0.0;
    }
    return Double.valueOf((days * 24.0 + hours) * 60.0 * 60.0 * 1000.0).longValue();
  }
  
  
}
