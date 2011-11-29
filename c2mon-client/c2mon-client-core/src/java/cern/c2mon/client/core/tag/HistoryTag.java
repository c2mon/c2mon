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
package cern.c2mon.client.core.tag;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.common.tag.TypeNumeric;
import cern.c2mon.client.core.tag.history.HistoryTagConfiguration;
import cern.c2mon.client.core.tag.history.HistoryTagExpressionException;
import cern.c2mon.client.core.tag.history.HistoryTagManager;
import cern.c2mon.client.core.tag.history.HistoryTagManagerListener;
import cern.c2mon.client.core.tag.history.HistoryTagParameter;
import cern.c2mon.client.core.tag.history.HistoryTagRecord;
import cern.c2mon.client.core.tag.history.HistoryTagResultType;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.tag.TagMode;
import cern.tim.shared.common.datatag.DataTagQuality;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.shared.common.datatag.TagQualityStatus;
import cern.tim.shared.rule.RuleExpression;

/**
 * This class takes an expression, and loads the data specified. An expression
 * can be created using {@link HistoryTagConfiguration#createExpression()}
 * 
 * @author vdeila
 */
public class HistoryTag implements ClientDataTagValue, DataTagUpdateListener, HistoryTagManagerListener {

  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(HistoryTag.class);
  
  /** The data which is shown to the user. */
  private ArrayList<HistoryTagRecord> data = null;
  
  /** Lock for {@link #data} */
  private final ReentrantReadWriteLock dataLock = new ReentrantReadWriteLock();
  
  /** The current value. <code>null</code> if it needs to be recalculated */
  private Object value = null;
  
  /** The list of update listeners */
  private List<DataTagUpdateListener> dataTagUpdateListeners;

  /** The quality of the history tag */
  private DataTagQuality dataTagQuality;
  
  /** The history mode */
  private TagMode historyMode = TagMode.OPERATIONAL;
  
  /** The tag name */
  private String name = "UNKNOWN";
  
  /** Indicates when the history data was updated for the last time */
  private Timestamp timestamp = null;
  
  /** The history tag manager */
  private final HistoryTagManager historyTagManager;
  
  /** The history tag configuration for this history tag */
  private final HistoryTagConfiguration configuration;
  
  /** The expression */
  private final String expression;
  
  /**
   * @param expression
   *          an expression created with the
   *          {@link HistoryTagConfiguration#createExpression()}
   */
  public HistoryTag(final String expression) {
    this.dataTagUpdateListeners = new ArrayList<DataTagUpdateListener>();
    this.dataTagQuality = new DataTagQualityImpl();
    this.expression = expression;
    
    HistoryTagConfiguration historyTagConfiguration;
    try {
      historyTagConfiguration = HistoryTagConfiguration.valueOf(expression);
    }
    catch (HistoryTagExpressionException e) {
      historyTagConfiguration = null;
      this.data = new ArrayList<HistoryTagRecord>();
      LOG.error(String.format("The history expression '%s' is invalid. ", expression), e);
    }
    this.configuration = historyTagConfiguration;
    
    if (this.configuration != null 
        && this.configuration.validate()) {
      this.timestamp = new Timestamp(System.currentTimeMillis());
      this.dataTagQuality.addInvalidStatus(TagQualityStatus.EQUIPMENT_DOWN, "Loading data");
      this.historyTagManager = HistoryTagManager.getInstance();
      this.historyTagManager.subscribe(this.configuration, this);
    }
    else {
      this.historyTagManager = null;
      this.dataTagQuality.addInvalidStatus(TagQualityStatus.UNDEFINED_TAG, "The expression is invalid");
    }
  }
  
  @Override
  public void onCancelled(final HistoryTagConfiguration filter) {
    synchronized (this) {
      this.dataTagQuality.validate();
      this.dataTagQuality.addInvalidStatus(TagQualityStatus.UNINITIALISED, "Failed to load the data");
      this.dataLock.writeLock().lock();
      try {
        this.value = null;
        this.data = null;
      }
      finally {
        this.dataLock.writeLock().unlock();
      }
      this.timestamp = new Timestamp(System.currentTimeMillis());
    }
    fireDataTagUpdateListeners();
  }

  @Override
  public void onLoaded(final HistoryTagConfiguration filter, final Collection<HistoryTagRecord> data) {
    synchronized (this) {
      this.dataTagQuality.validate();
      this.dataLock.writeLock().lock();
      try {
        this.data = new ArrayList<HistoryTagRecord>(data);
        this.value = null;
      }
      finally {
        this.dataLock.writeLock().unlock();
      }
      this.timestamp = new Timestamp(System.currentTimeMillis());
    }
    fireDataTagUpdateListeners();
  }
  
  @Override
  public void onUpdate(final ClientDataTagValue tagUpdate) {
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
        if (configuration.getDays() != null) {
          final Calendar calendar = Calendar.getInstance();
          calendar.setTimeInMillis(System.currentTimeMillis());
          calendar.add(Calendar.DAY_OF_YEAR, -configuration.getDays());
          final Date startDate = calendar.getTime();
          while (startDate.compareTo(data.get(0).getTimestamp()) > 0
              && data.size() > 0) {
            data.remove(0);
          }
        }
        value = null;
      }
    }
    finally {
      this.dataLock.writeLock().unlock();
    }
    fireDataTagUpdateListeners();
  }
  
  /**
   * 
   * @return the tag ids that is used by the history tag.
   */
  public Collection<Long> getTagIds() {
    return Arrays.asList(this.configuration.getTagId());
  }
  
  /**
   * @return the data
   */
  public Collection<HistoryTagRecord> getData() {
    this.dataLock.readLock().lock();
    try {
      return new ArrayList<HistoryTagRecord>(this.data);
    }
    finally {
      this.dataLock.readLock().unlock();
    }
  }
  
  @Override
  public Object getValue() {
    Object result;
    this.dataLock.readLock().lock();
    try {
      result = this.value;
    }
    finally {
      this.dataLock.readLock().unlock();
    }
    if (result == null) {
      result = recalculateValue();
    }
    return result;
  }
  
  /**
   * Recalculates the value based on {@link #configuration} and the
   * {@link #data}
   * 
   * @return the updates {@link #value}
   */
  private Object recalculateValue() {
    this.dataLock.writeLock().lock();
    try {
      if (this.value == null && this.data != null) {
        if (this.configuration.getResultType().getResultClass().isArray()) {
          final List<Object> values = new ArrayList<Object>(this.data.size());
          for (HistoryTagRecord record : this.data) {
            switch (this.configuration.getResultType()) {
            case Labels:
              values.add(record.getTimestamp());
              break;
            case Values:
              values.add(record.getValue());
              break;
            default:
              LOG.error(String.format("The %s '%s' is not supported by the '%s' class", 
                  HistoryTagResultType.class.getSimpleName(),
                  this.configuration.getResultType().toString(),
                  HistoryTag.class.getSimpleName()));
              throw new RuntimeException("Invalid type, see the log for details");
            }
          }
          this.value = values.toArray();
        }
        else {
          switch (this.configuration.getResultType()) {
          default:
            LOG.error(String.format("The %s '%s' is not supported by the '%s' class", 
                HistoryTagResultType.class.getSimpleName(),
                this.configuration.getResultType().toString(),
                HistoryTag.class.getSimpleName()));
            throw new RuntimeException("Invalid type, see the log for details");
          }
        }
      }
      return this.value;
    }
    finally {
      this.dataLock.writeLock().unlock();
    }
  }
  
  /**
   * Unregisters all that it have registered itself to.
   */
  private void unregister() {
    if (this.historyTagManager != null) {
      this.historyTagManager.unsubscribe(configuration, this);
    }
  }
  
  @Override
  public DataTagQuality getDataTagQuality() {
    return dataTagQuality;
  }
  
  @Override
  public Long getId() {
//    if (this.historyTagConfiguration != null) {
//      return this.historyTagConfiguration.getTagId();
//    }
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
  public synchronized void addDataTagUpdateListener(final DataTagUpdateListener listener) {
    dataTagUpdateListeners.add(listener);
    listener.onUpdate(this);
  }
  
  /**
   * @param listener the listener to remove
   */
  public synchronized void removeDataTagUpdateListener(final DataTagUpdateListener listener) {
    dataTagUpdateListeners.remove(listener);
    if (dataTagUpdateListeners.isEmpty()) {
      unregister();
    }
  }
  
  /**
   * @return a copy of the list of listeners
   */
  private synchronized Collection<DataTagUpdateListener> getDataTagUpdateListeners() {
   return new ArrayList<DataTagUpdateListener>(this.dataTagUpdateListeners);
  }
  
  /**
   * Fires the {@link DataTagUpdateListener#onUpdate(ClientDataTagValue)} on all the listeners
   */
  private void fireDataTagUpdateListeners() {
    for (DataTagUpdateListener listener : getDataTagUpdateListeners()) {
      listener.onUpdate(this);
    }
  }
}
