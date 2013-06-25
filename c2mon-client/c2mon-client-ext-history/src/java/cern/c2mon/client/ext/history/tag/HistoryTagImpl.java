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
package cern.c2mon.client.ext.history.tag;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.common.tag.TypeNumeric;
import cern.c2mon.client.ext.history.common.tag.HistoryTag;
import cern.c2mon.client.ext.history.common.tag.HistoryTagConfiguration;
import cern.c2mon.client.ext.history.common.tag.HistoryTagExpressionException;
import cern.c2mon.client.ext.history.common.tag.HistoryTagParameter;
import cern.c2mon.client.ext.history.common.tag.HistoryTagRecord;
import cern.c2mon.client.ext.history.common.tag.HistoryTagResultType;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.tag.TagMode;
import cern.tim.shared.common.datatag.DataTagQuality;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.shared.rule.RuleExpression;

/**
 * This class takes an expression, and loads the data specified. An expression
 * can be created using {@link HistoryTagConfiguration#createExpression()}<br/>
 * <br/>
 * After a history tag is created it must be subscribed for data tag updates,
 * and for history data.
 * 
 * @see HistoryTagManager#subscribe(HistoryTagImpl)
 * @see C2monTagManager#subscribeDataTags(java.util.Set, DataTagUpdateListener)
 * 
 * @author vdeila
 */
public class HistoryTagImpl implements HistoryTag {

  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(HistoryTagImpl.class);
  
  /** The data which is shown to the user. */
  private ArrayList<HistoryTagRecord> data = null;
  
  /** Lock for {@link #data} */
  private final ReentrantReadWriteLock dataLock = new ReentrantReadWriteLock();
  
  /** The current value. <code>null</code> if it needs to be recalculated */
  private Object value;
  
  /** The list of update listeners */
  private List<DataTagUpdateListener> dataTagUpdateListeners;

  /** The quality of the history tag */
  private DataTagQuality dataTagQuality;
  
  /** The history mode */
  private TagMode historyMode = TagMode.OPERATIONAL;
  
  /** The tag name */
  private String name = "UNKNOWN";
  
  /** Indicates when the history data was updated for the last time */
  private Timestamp timestamp;
  
  /** The history tag configuration for this history tag */
  private final HistoryTagConfiguration configuration;
  
  /** The expression that defines what data to show to the user */
  private final String expression;
  
  /** The class that extracts the data that is returned through {@link #getValue()} */
  private final HistoryTagRecordConverter dataConverter;

  /** The last time the value were calculated (using {@link System#currentTimeMillis()}) */
  private long lastCalculatedTime = 0;
  
  /** Lock for {@link #value} and {@link #lastCalculatedTime} */
  private final ReentrantReadWriteLock valueLock = new ReentrantReadWriteLock();
  
  /**
   * The history tag must be subscribed for data tag updates, and for history
   * data.
   * 
   * @see HistoryTagManager#subscribe(HistoryTagImpl)
   * @see C2monTagManager#subscribeDataTags(java.util.Set,
   *      DataTagUpdateListener)
   * 
   * @param expression
   *          an expression created with the
   *          {@link HistoryTagConfiguration#createExpression()}
   */
  public HistoryTagImpl(final String expression) {
    this(createHistoryTagConfiguration(expression), expression, true);
  }
  
  /**
   * The history tag must be subscribed for data tag updates, and for history
   * data.
   * 
   * @see HistoryTagManager#subscribe(HistoryTagImpl)
   * @see C2monTagManager#subscribeDataTags(java.util.Set,
   *      DataTagUpdateListener)
   * @param expression
   *          an expression created with the
   *          {@link HistoryTagConfiguration#createExpression()}
   * @param allowNullValues Whether Null values are allowed as a result, or should be removed.
   */
  public HistoryTagImpl(final String expression, final boolean allowNullValues) {
    this(createHistoryTagConfiguration(expression), expression, allowNullValues);
  }
  
  /**
   * @param historyTagConfiguration
   *          the configuration to use for the history tag
   */
  public HistoryTagImpl(final HistoryTagConfiguration historyTagConfiguration) {
    this(historyTagConfiguration, null, true);
  }

  /**
   * @param historyTagConfiguration
   *          the configuration to use for the history tag
   * @param expression
   *          the expression used to create the configuration, can be
   *          <code>null</code>
   * @param allowNullValues Whether Null values are allowed as a result, or should be removed.
   */
  public HistoryTagImpl(final HistoryTagConfiguration historyTagConfiguration, final String expression
       , final boolean allowNullValues) {
    
    this.dataTagUpdateListeners = new ArrayList<DataTagUpdateListener>();
    this.data = null;
    this.value = null;
    this.timestamp = new Timestamp(System.currentTimeMillis());
    this.dataTagQuality = new DataTagQualityImpl();
    this.dataTagQuality.validate();
    this.dataConverter = new HistoryTagRecordConverter(this, allowNullValues);
    
    if (expression == null && historyTagConfiguration != null) {
      String generatedExpression;
      try {
        generatedExpression = historyTagConfiguration.createExpression();
      }
      catch (HistoryTagExpressionException e) {
        generatedExpression = "Unknown";
      }
      this.expression = generatedExpression;
    }
    else {
      this.expression = expression;
    }
    
    // Check that the configuration is valid
    if (historyTagConfiguration != null 
        && historyTagConfiguration.validate()) {
      
      // The data is set to invalid, as it is not yet loaded. 
      this.dataTagQuality.addInvalidStatus(QUALITY_STATUS_LOADING, "Loading data");
      
      this.configuration = historyTagConfiguration;
    }
    else {
      // Could't load the data, set the tag to invalid.
      this.configuration = null;
      this.dataTagQuality.addInvalidStatus(QUALITY_STATUS_EXPRESSION_ERROR, "The expression is invalid");
    }
  }
  
  
  /**
   * @param expression
   *          the expression to create a configuration from
   * @return the configuration of the expression, or <code>null</code> if it
   *         failed to create it.
   */
  private static HistoryTagConfiguration createHistoryTagConfiguration(final String expression) {
    // Extract the configuration from the given expression
    try {
      return HistoryTagConfigurationImpl.valueOf(expression);
    }
    catch (HistoryTagExpressionException e) {
      LOG.error(String.format("The history expression '%s' is invalid. ", expression), e);
    }
    return null;
  }
  
  @Override
  public void onCancelled(final HistoryTagConfiguration filter) {
    // Loading the data from history failed or was canceled. Invalidating the tag.
    synchronized (this.dataTagQuality) {
      this.dataTagQuality.validate();
      this.dataTagQuality.addInvalidStatus(QUALITY_STATUS_FAILED, "Failed to load the data");
    }
    this.dataLock.writeLock().lock();
    try {
      this.data = null;
      this.timestamp = new Timestamp(System.currentTimeMillis());
      
      this.valueLock.writeLock().lock();
      try {
        this.value = null;
      }
      finally {
        this.valueLock.writeLock().unlock();
      }
    }
    finally {
      this.dataLock.writeLock().unlock();
    }
    fireDataTagUpdateListeners();
  }

  @Override
  public void onLoaded(final HistoryTagConfiguration filter, final Collection<HistoryTagRecord> data) {
    // The data was succsessfully loaded from the database 
    synchronized (this.dataTagQuality) {
      this.dataTagQuality.validate();
    }
    
    this.dataLock.writeLock().lock();
    try {
      this.data = new ArrayList<HistoryTagRecord>(data);
      this.timestamp = new Timestamp(System.currentTimeMillis());
      
      this.valueLock.writeLock().lock();
      try {
        this.value = null;
      }
      finally {
        this.valueLock.writeLock().unlock();
      }
    }
    finally {
      this.dataLock.writeLock().unlock();
    }
    fireDataTagUpdateListeners();
  }
  
  @Override
  public void onUpdate(final ClientDataTagValue tagUpdate) {
    // When a new tag update comes from the c2mon server.
    this.dataLock.writeLock().lock();
    try {
      if (data != null) {
        data.add(new HistoryTagRecord(tagUpdate));
        if (configuration.getRecords() != null) {
          while (data.size() > configuration.getRecords()
              && data.size() > 0) {
            data.remove(0);
          }
        }
        if (configuration.getTotalMilliseconds() != null) {
          final Timestamp startDate = new Timestamp(System.currentTimeMillis() - configuration.getTotalMilliseconds());
          while (data.size() > 0
              && startDate.compareTo(data.get(0).getTimestamp()) > 0) {
            data.remove(0);
          }
        }
        this.timestamp = new Timestamp(System.currentTimeMillis());
        
        this.valueLock.writeLock().lock();
        try {
          value = null;
        }
        finally {
          this.valueLock.writeLock().unlock();
        }
      }
    }
    finally {
      this.dataLock.writeLock().unlock();
    }
    fireDataTagUpdateListeners();
  }
  
  /**
   * @return the tag ids that is used by the history tag.
   */
  public Collection<Long> getTagIds() {
    if (this.configuration != null) {
      return Arrays.asList(this.configuration.getTagId());
    }
    else {
      return Collections.emptyList();
    }
  }
  
  @Override
  public Collection<HistoryTagRecord> getCurrentData(final HistoryTagConfiguration configuration) {
    this.dataLock.readLock().lock();
    try {
      if (this.data != null) {
        return new ArrayList<HistoryTagRecord>(this.data);
      }
      else {
        return null;
      }
    }
    finally {
      this.dataLock.readLock().unlock();
    }
  }
  
  @Override
  public Object getValue() {
    Object result;
    this.valueLock.readLock().lock();
    try {
      result = this.value;
    }
    finally {
      this.valueLock.readLock().unlock();
    }
    if (result == null) {
      result = recalculateValue();
    }
    return result;
  }
  
  /**
   * Recalculates the value based on {@link #configuration} and the
   * {@link #data} using {@link HistoryTagRecordConverter#convert(Collection)}
   * 
   * @return the updates {@link #value}
   */
  private Object recalculateValue() {
    if (this.configuration != null) {
      // The time of when the calculation started
      final long dataTime = System.currentTimeMillis();
      final Object newValue = this.dataConverter.convert();
      
      this.valueLock.writeLock().lock();
      try {
        if (this.lastCalculatedTime < dataTime) {
          this.lastCalculatedTime = dataTime;
          this.value = newValue;
        }
      }
      finally {
        this.valueLock.writeLock().unlock();
      }
      
      return newValue;
    }
    else {
      return null;
    }
  }
  
  @Override
  public DataTagQuality getDataTagQuality() {
    return dataTagQuality;
  }
  
  @Override
  public Long getId() {
    return -1L;
  }

  @Override
  public Collection<Long> getAlarmIds() {
    return Collections.emptyList();
  }

  @Override
  public Collection<AlarmValue> getAlarms() {
    return Collections.emptyList();
  }

  @Override
  public String getDescription() {
    return String.format("History data: '%s'", this.expression);
  }

  @Override
  public Collection<Long> getEquipmentIds() {
    return Collections.emptyList();
  }

  @Override
  public TagMode getMode() {
    return historyMode;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Collection<Long> getProcessIds() {
    return Collections.emptyList();
  }

  @Override
  public RuleExpression getRuleExpression() {
    return null;
  }

  @Override
  public Timestamp getServerTimestamp() {
    return null;
  }
  
  @Override
  public Timestamp getDaqTimestamp() {
    return null;
  }

  @Override
  public synchronized Timestamp getTimestamp() {
    if (timestamp == null) {
      return new Timestamp(0L);
    }
    return timestamp;
  }

  @Override
  public Class< ? > getType() {
    final HistoryTagResultType resultType = this.configuration.getValue(HistoryTagParameter.Result, HistoryTagResultType.class);
    if (resultType != null) {
      return resultType.getResultClass();
    }
    else {
      return Object.class;
    }
  }

  @Override
  public TypeNumeric getTypeNumeric() {
    Class< ? > type = getType();
    if (type != null) {
      int typeNumeric = type.hashCode();
      for (TypeNumeric t : TypeNumeric.values()) {
        if (t.getCode() == typeNumeric) {
          return t;
        }
      }
    }
    return TypeNumeric.TYPE_UNKNOWN;
  }

  @Override
  public String getUnit() {
    return "";
  }

  @Override
  public String getValueDescription() {
    return "History tag";
  }

  @Override
  public boolean isRuleResult() {
    return false;
  }

  @Override
  public boolean isSimulated() {
    return false;
  }

  @Override
  public boolean isValid() {
    return this.dataTagQuality.isValid();
  }
  
  /** 
   * @param listener the listener to add
   */
  @Override
  public synchronized void addDataTagUpdateListener(final DataTagUpdateListener listener) {
    dataTagUpdateListeners.add(listener);
    listener.onUpdate(this);
  }
  
  /**
   * @param listener the listener to remove
   */
  @Override
  public synchronized void removeDataTagUpdateListener(final DataTagUpdateListener listener) {
    dataTagUpdateListeners.remove(listener);
  }
  
  /**
   * @return a copy of the list of listeners
   */
  public synchronized Collection<DataTagUpdateListener> getDataTagUpdateListeners() {
   return new ArrayList<DataTagUpdateListener>(this.dataTagUpdateListeners);
  }
  
  /**
   * Removes all listeners.
   */
  public synchronized void removeDataTagUpdateListeners() {
    this.dataTagUpdateListeners.clear();
  }
  
  /**
   * Fires the {@link DataTagUpdateListener#onUpdate(ClientDataTagValue)} on all the listeners
   */
  private void fireDataTagUpdateListeners() {
    for (DataTagUpdateListener listener : getDataTagUpdateListeners()) {
      listener.onUpdate(this);
    }
  }

  /**
   * @return the history tag configuration
   */
  @Override
  public HistoryTagConfiguration getConfiguration() {
    return configuration;
  }

  /**
   * @return the expression for this tag.
   */
  @Override
  public String getExpression() {
    return expression;
  }
}
