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

import java.util.HashMap;
import java.util.Map;

import cern.c2mon.client.core.tag.HistoryTag;

/**
 * This is a configuration for a {@link HistoryTag}. Can be used to create an
 * expression for a history tag.
 * 
 * @see #createExpression()
 * @see HistoryTagExpressionInterpreter
 * @see HistoryTag
 * 
 * @author vdeila
 */
public class HistoryTagConfiguration {
  
  /** The values */
  private final Map<HistoryTagParameter, Object> values;
  
  /**
   * Creates an instance with default values (Mostly <code>null</code> values)
   */
  public HistoryTagConfiguration() {
    this.values = new HashMap<HistoryTagParameter, Object>();
    for (HistoryTagParameter parameter : HistoryTagParameter.values()) {
      if (parameter.getDefaultValue() != null) {
        this.values.put(parameter, parameter.getDefaultValue());
      }
    }
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
    return this.values.get(parameter);
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
      final Object value = this.values.get(parameter);
      if (value == null) {
        return clazz.cast(parameter.getDefaultValue());
      }
      if (clazz.isInstance(value)) {
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
    if (value != null && !parameter.getType().isInstance(value)) {
      throw new ClassCastException(String.format("The value of type '%s' cannot be set for the %s '%s' because it expects a value of type '%s'", 
          value.getClass().getName(), 
          HistoryTagParameter.class.getSimpleName(), 
          parameter.toString(), 
          parameter.getType().getName()));
    }
    if (value != null) {
      this.values.put(parameter, value);
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
    if (values.get(HistoryTagParameter.Result) == null) {
      throw new HistoryTagExpressionException("A result type must be set!");
    }
    if (values.get(HistoryTagParameter.TagId) == null) {
      throw new HistoryTagExpressionException("The 'tagId' cannot be null");
    }
    if (values.get(HistoryTagParameter.Records) == null && values.get(HistoryTagParameter.Days) == null) {
      throw new HistoryTagExpressionException("Either 'records' or 'days' must be set!");
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
      final Object value = this.values.get(parameter);
      if (value != null) {
        if (parameter.getArgument() != null) {
          expr.append(parameter.getArgument());
          expr.append("=");
        }
        expr.append(value.toString());
      }
      expr.append(' ');
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
   *         comparing only parameters that have
   *         {@link HistoryTagParameter#isAffectingQuery()} set to
   *         <code>true</code>.
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof HistoryTagConfiguration))
      return false;
    final HistoryTagConfiguration other = (HistoryTagConfiguration) obj;
    for (HistoryTagParameter parameter : HistoryTagParameter.values()) {
      if (parameter.isAffectingQuery()) {
        final Object myValue = this.getValue(parameter);
        final Object otherValue = other.getValue(parameter);
        if (myValue != otherValue
            && myValue != null
            && !myValue.equals(otherValue)) {
          return false;
        }
      }
    }
    return true;
  };
  
  /**
   * @return the hash code, only including the parameters which have
   *         {@link HistoryTagParameter#isAffectingQuery()} set to
   *         <code>true</code>.
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    for (HistoryTagParameter parameter : HistoryTagParameter.values()) {
      if (parameter.isAffectingQuery()) {
        final Object value = this.getValue(parameter);
        result = prime * result + ((value == null) ? 0 : value.hashCode());
      }
    }
    return result;
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
   * @return The maximum number of days that will be retrieved 
   */
  public Integer getDays() {
    return getValue(HistoryTagParameter.Days, Integer.class);
  }
  
  /**
   * @return the type of result that is requested
   */
  public HistoryTagResultType getResultType() {
    return getValue(HistoryTagParameter.Result, HistoryTagResultType.class);
  }
  
  
}
