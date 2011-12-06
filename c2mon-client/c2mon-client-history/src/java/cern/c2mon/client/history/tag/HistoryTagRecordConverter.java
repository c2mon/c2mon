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

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.nfunk.jep.JEP;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

import cern.c2mon.client.common.history.tag.HistoryTag;
import cern.c2mon.client.common.history.tag.HistoryTagConfiguration;
import cern.c2mon.client.common.history.tag.HistoryTagParameter;
import cern.c2mon.client.common.history.tag.HistoryTagRecord;
import cern.c2mon.client.common.history.tag.HistoryTagResultType;
import cern.tim.shared.common.datatag.DataTagQuality;
import cern.tim.shared.common.type.TypeConverter;

/**
 * This class provides functions to convert a collection of
 * {@link HistoryTagRecord}s into what the configuration of the
 * {@link HistoryTag} provided.
 * 
 * @author vdeila
 */
final class HistoryTagRecordConverter {

  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(HistoryTagRecordConverter.class);

  /** The history tag that this convertion is for */
  private final HistoryTag historyTag;
  
  /** the data to convert */
  private Collection<HistoryTagRecord> data = null;
  
  /**
   * @param historyTag the history tag to convert the data of
   */
  public HistoryTagRecordConverter(final HistoryTag historyTag) {
    this.historyTag = historyTag;
  }

  /**
   * @return the converted {@link HistoryTag#getData()}. That will be of the
   *         type {@link HistoryTagResultType#getResultClass()} of the
   *         {@link HistoryTagConfiguration#getResultType()}
   * @throws RuntimeException
   *           if the configuration retrieved from {@link HistoryTag#getConfiguration()} is not supported
   */
  public synchronized Object convert() throws RuntimeException {
    this.data = historyTag.getCurrentData(historyTag.getConfiguration());
    try {
      Object result = convertToResultType(historyTag.getConfiguration().getResultType());
      result = changeResult(result, historyTag.getConfiguration().getValue(HistoryTagParameter.ResultChange, String.class));
      return result;
    }
    finally {
      this.data = null;
    }
  }
  
  /**
   * @param resultType the result type to return
   * @return the converted result based on the {@link #historyTag}  
   * @throws RuntimeException
   *           if the configuration specified is not supported
   */
  private Object convertToResultType(final HistoryTagResultType resultType) throws RuntimeException {
    Object result = null;
    
    switch (resultType) {
    case Conditional:
      final DataTagQuality quality = historyTag.getDataTagQuality();
      if (quality.isInvalidStatusSet(HistoryTag.QUALITY_STATUS_LOADING)) {
        result = historyTag.getConfiguration().getValue(HistoryTagParameter.LoadingValue);
      }
      else if (quality.isValid()) {
        result = historyTag.getConfiguration().getValue(HistoryTagParameter.ActiveValue);
      }
      else {
        result = historyTag.getConfiguration().getValue(HistoryTagParameter.FailedValue);
      }
      break;
    case Labels:
      result = getXValues(data).toArray();
      break;
    case Values:
      result = getYValues(data).toArray();
      break;
    case XMax: 
      result = findValue(getXValues(data), true, resultType.getResultClass());
      break;
    case XMin:
      result = findValue(getXValues(data), false, resultType.getResultClass());
      break;
    case YMax:
      result = findValue(getYValues(data), true, resultType.getResultClass());
      break;
    case YMin:
      result = findValue(getYValues(data), false, resultType.getResultClass());
      break;
    default:
      LOG.error(String.format("The %s '%s' is not supported", HistoryTagResultType.class.getSimpleName(), resultType.toString()));
      throw new RuntimeException("Invalid type, see the log for details");
    }
    
    // Converts the data to be of the correct array type
    result = TypeConverter.cast(result, resultType.getResultClass());
    
    return result;
  }
  
  /**
   * Changes the data based on the <code>changeExpression</code>
   * 
   * @param data the data to change
   * @param changeExpression the expression that defines how the data should be changed.
   * @return the changed data
   */
  private Object changeResult(final Object data, final String changeExpression) {
    if (changeExpression == null) {
      return data;
    }
    
    Object result;
    
    if (data != null && data.getClass().isArray()) {
      final int arrayLength = Array.getLength(data);
      final Object[] array = new Object[arrayLength];
      
      for (int i = 0; i < arrayLength; i++) {
        Array.set(array, i, changeResult(Array.get(data, i), changeExpression));
      }
      result = array;
    }
    else {
      final JEP jep = new JEP();
      try {
        if (data == null) {
          jep.addVariable("x", 0.0);
        }
        else {
          jep.addVariable("x", TypeConverter.castToType(data, double.class));
        }
        jep.addFunction("formatTime", new DateFormatFunction());
        jep.addFunction("concat", new ConcatFunction());
        for (HistoryTagResultType resultType : HistoryTagResultType.values()) {
          jep.addFunction(resultType.toString().toLowerCase(), new ChangeFunction(resultType));
        }
        jep.parseExpression(changeExpression);
        result = jep.getValueAsObject();
      } catch (Exception e) {
        final String dataStr;
        if (data == null) {
          dataStr = "null";
        }
        else {
          dataStr = data.toString();
        }
        final String errorMessage = String.format(
            "The expression '%s' could not be evaluated where x is '%s'",
            changeExpression, dataStr
            );
        LOG.error(errorMessage, e);
        throw new RuntimeException(errorMessage, e);
      }
    }
    
    return result;
  }
  
  /** Function available from the jep expressions */
  private class ChangeFunction extends PostfixMathCommand {
    /** The result type */
    private final HistoryTagResultType resultType;
    /** @param resultType the result type */
    public ChangeFunction(final HistoryTagResultType resultType) {
      super();
      this.resultType = resultType;
    }

    @Override
    public int getNumberOfParameters() {
      return 0;
    }

    @Override
    public void run(final Stack stack) throws ParseException {
      stack.push(convertToResultType(this.resultType));
    }
  }
  
  /** Function for formatting a Timestamp */
  private static class DateFormatFunction extends PostfixMathCommand {
    public DateFormatFunction() {
      numberOfParameters = 2;
    }

    @Override
    public void run(final Stack stack) throws ParseException {
      checkStack(stack);
      final Timestamp date;
      final String newFormat;
      try {
        newFormat = TypeConverter.castToType(stack.pop(), String.class);
      }
      catch (Exception e) {
        throw new ParseException("The second argument must be a String!");
      }
      try {
        date =  TypeConverter.castToType(stack.pop(), Timestamp.class);
      }
      catch (Exception e) {
        throw new ParseException("The first argument must be a Timestamp!");
      }
      
      stack.push(new SimpleDateFormat(newFormat).format(date));
    }
  }
  
  /** Function for concatting two values */
  private static class ConcatFunction extends PostfixMathCommand {
    public ConcatFunction() {
      numberOfParameters = 2;
    }

    @Override
    public void run(final Stack stack) throws ParseException {
      checkStack(stack);
      
      Object value2 = stack.pop();
      Object value1 = stack.pop();
      
      if (value1 == null) {
        value1 = "null";
      }
      if (value2 == null) {
        value2 = "null";
      }
      final String result = value1.toString().concat(value2.toString());
      
      stack.push(result);
    }
  }
  
  /**
   * @param <T> the return type, must be comparable
   * @param data the data to find the maximum or minimum of
   * @param findMax <code>true</code> to find the maximum, <code>false</code> to find the minimum.
   * @param resultClass the return type
   * @return the maximum or minimum value from the <code>data</code> collection
   */
  @SuppressWarnings("unchecked")
  private static <T> T findValue(final Collection< ? > data, final boolean findMax, final Class<T> resultClass) {
    try {
      return (T) findValueComparable(data, findMax, resultClass.asSubclass(Comparable.class));
    }
    catch (ClassCastException e) {
      throw new RuntimeException(String.format(
                    "The value of type '%s' is not '%s'.",
                    resultClass.getName(),
                    Comparable.class.getSimpleName()),
                    e);
    }
    catch (Exception e) {
      throw new RuntimeException(String.format(
          "Failed to get the max or min value of type '%s'",
          resultClass.getName()),
          e);
    }
  }
  
  /**
   * @param <T> the return type, must be comparable
   * @param data the data to find the maximum or minimum of
   * @param findMax <code>true</code> to find the maximum, <code>false</code> to find the minimum.
   * @param comparableResultClass the return type
   * @return the maximum or minimum value from the <code>data</code> collection
   */
  private static <T extends Comparable<T>> T findValueComparable(final Collection< ? > data, final boolean findMax, final Class<T> comparableResultClass) {
    T result = null;
    if (data != null) {
      for (Object object : data) {
        if (object == null) {
          continue;
        }
        final T value = TypeConverter.castToType(object, comparableResultClass);
        if (result == null) {
          result = value;
        }
        else {
          if (findMax && value.compareTo(result) > 0
              || !findMax && value.compareTo(result) < 0) {
            result = value;
          }
        }
      }
    }
    return result;
  }
  
  /**
   * @param records
   *          the records to get the x data from.
   * @return all the x values of the records. That is all the
   *         {@link HistoryTagRecord#getTimestamp()}s.
   */
  private static Collection<Timestamp> getXValues(final Collection<HistoryTagRecord> records) {
    final List<Timestamp> result = new ArrayList<Timestamp>();
    if (records != null) {
      for (final HistoryTagRecord record : records) {
        result.add(record.getTimestamp());
      }
    }
    return result;
  }
  
  /**
   * @param records
   *          the records to get the y data from.
   * @return all the y values of the records. That is all the
   *         {@link HistoryTagRecord#getValue()}s.
   */
  private static Collection<Object> getYValues(final Collection<HistoryTagRecord> records) {
    final List<Object> result = new ArrayList<Object>();
    if (records != null) {
      for (final HistoryTagRecord record : records) {
        result.add(record.getValue());
      }
    }
    return result;
  }

}
