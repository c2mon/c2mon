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
package cern.c2mon.client.history.playback.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cern.c2mon.client.common.history.HistoryUpdate;
import cern.c2mon.client.common.history.id.HistoryUpdateId;
import cern.c2mon.client.common.history.id.TagValueUpdateId;
import cern.c2mon.client.history.data.event.HistoryStoreListener;
import cern.c2mon.client.history.data.utilities.HistoryDataUtil;
import cern.c2mon.client.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.client.history.util.HistoryGroup;
import cern.c2mon.shared.client.tag.TagValueUpdate;

/**
 * This class holds all history data which is loaded by {@link HistoryLoader}
 * 
 * @see HistoryStoreListener
 * 
 * @author vdeila
 */
public class HistoryStore {

  /** A list of data ids which will be filtered out */
  private static final HistoryUpdateId[] ILLEGAL_IDS = new HistoryUpdateId[] { 
    new TagValueUpdateId(-1L), new TagValueUpdateId(0L) };
  
  /** A map containing all history for all the objects */
  private Map<HistoryUpdateId, HistoryGroup> dataHistories;

  /** A lock for dataTagHistories map */
  private ReentrantReadWriteLock dataTagHistoriesLock;

  /** The history player's start date */
  private Timestamp start;

  /** The history player's start date */
  private Timestamp end;

  /** A list of action listeners */
  private List<HistoryStoreListener> historyStoreListeners;

  /** A lock for historyStoreListeners list */
  private ReentrantReadWriteLock historyStoreListenersLock;

  /**
   * A map with the dataIds, each containing the ending time for how far the
   * records have been retrieved. All data which can be loaded will be in this
   * list, even just with the start time as timestamp.
   */
  private Map<HistoryUpdateId, Timestamp> tagsHaveRecordsUntilTime;

  /**
   * A lock for tagsHaveRecordsUntilTime list. The {@link #initializedTagsLock}
   * must be held before locking this lock if used at the same time.
   */
  private ReentrantReadWriteLock tagsHaveRecordsUntilTimeLock;

  /** 
   * Data that have been initialized, i.e. first value is loaded.
   * A tag will not be loaded before it is initialized.
   */
  private Set<HistoryUpdateId> initializedTags;

  /** Lock for {@link #initializedTags} */
  private ReentrantReadWriteLock initializedTagsLock;

  /**
   * <code>true</code> if any records or DataTagHistorys have been added or
   * removed.
   */
  private boolean recordsLoadedUntilTimeIsDirty;

  /**
   * The ending time of how far in time the records have been loaded. Is updated
   * by <code>updateTimeRecordsIsLoadedUntil()</code>
   */
  private Timestamp recordsLoadedUntilTime;

  /**
   * Lock for recordsLoadedUntilTime and recordsLoadedUntilTimeIsDirty
   */
  private ReentrantReadWriteLock recordsLoadedUntilTimeLock;

  /**
   * If someone executes a batch which adds many records etc. in a short periode
   * of time
   */
  private int batchesGoingOn;

  /**
   * Lock for batchesGoingOn
   */
  private ReentrantReadWriteLock batchesGoingOnLock;
  
  /** Postponed callbacks to {@link #fireOnObjectsInitialized(Collection)}} because it is batching */
  private final List<HistoryUpdateId> postponedFireOnObjectsInitialized;

  /** Postponed callbacks to {@link #fireOnObjectCollectionChanged(Collection)} because it is batching */
  private final List<HistoryUpdateId> postponedFireOnObjectCollectionChanged;

  /**
   * Constructor
   */
  public HistoryStore() {
    this.postponedFireOnObjectsInitialized = new ArrayList<HistoryUpdateId>();
    this.postponedFireOnObjectCollectionChanged = new ArrayList<HistoryUpdateId>();
    this.dataTagHistoriesLock = new ReentrantReadWriteLock();
    this.historyStoreListenersLock = new ReentrantReadWriteLock();
    this.tagsHaveRecordsUntilTimeLock = new ReentrantReadWriteLock();
    this.initializedTagsLock = new ReentrantReadWriteLock();
    this.recordsLoadedUntilTimeLock = new ReentrantReadWriteLock();
    this.batchesGoingOnLock = new ReentrantReadWriteLock();

    final long theTime = System.currentTimeMillis();
    this.start = new Timestamp(theTime);
    this.end = new Timestamp(theTime);

    this.dataHistories = new Hashtable<HistoryUpdateId, HistoryGroup>();
    this.historyStoreListeners = new ArrayList<HistoryStoreListener>();
    this.tagsHaveRecordsUntilTime = new HashMap<HistoryUpdateId, Timestamp>();
    this.initializedTags = new HashSet<HistoryUpdateId>();
    this.recordsLoadedUntilTime = null;
    this.recordsLoadedUntilTimeIsDirty = false;
    this.batchesGoingOn = 0;
  }

  /**
   * Clears all data in the history store
   */
  public void clear() {

    // Clears the dataTagHistories
    try {
      this.dataTagHistoriesLock.writeLock().lock();
      this.dataHistories.clear();
    }
    finally {
      this.dataTagHistoriesLock.writeLock().unlock();
    }

    // Clears the tagsHaveRecordsUntilTime
    try {
      this.tagsHaveRecordsUntilTimeLock.writeLock().lock();
      this.tagsHaveRecordsUntilTime.clear();
    }
    finally {
      this.tagsHaveRecordsUntilTimeLock.writeLock().unlock();
    }

    // Clears the initializedTags
    try {
      this.initializedTagsLock.writeLock().lock();
      this.initializedTags.clear();
    }
    finally {
      this.initializedTagsLock.writeLock().unlock();
    }

    // Clears the recordsLoadedUntilTime, recordsLoadedUntilTimeIsDirty
    try {
      this.recordsLoadedUntilTimeLock.writeLock().lock();
      this.recordsLoadedUntilTime = null;
      this.recordsLoadedUntilTimeIsDirty = false;
    }
    finally {
      this.recordsLoadedUntilTimeLock.writeLock().unlock();
    }

  }

  /**
   * Updates the buffer ending time (to which is loaded) and notifies listeners
   * if necessary
   * 
   * @return <code>true</code> if the time have changed, <code>false</code>
   *         otherwise. Does only compare data that is "accepted"!
   */
  private boolean updateTimeRecordsIsLoadedUntil() {
    Timestamp newTimestamp = null;

    // Checks if update of the timestamp is needed
    boolean needsUpdate = false;
    try {
      this.recordsLoadedUntilTimeLock.readLock().lock();
      needsUpdate = this.recordsLoadedUntilTimeIsDirty || this.recordsLoadedUntilTime == null;
    }
    finally {
      this.recordsLoadedUntilTimeLock.readLock().unlock();
    }

    if (needsUpdate) {
      try {
        this.recordsLoadedUntilTimeLock.writeLock().lock();

        // Keeps the old value to decide whether or not to notify the listeners
        // after updating the values
        final Timestamp recordsLoadedUntilTimeOldValue = this.recordsLoadedUntilTime;

        boolean haveAllValues = true;
        Timestamp leastLoadedTimestamp = this.getEnd();
        final HistoryUpdateId[] dataIds;
        try {
          this.tagsHaveRecordsUntilTimeLock.readLock().lock();
          dataIds = this.tagsHaveRecordsUntilTime.keySet().toArray(new HistoryUpdateId[0]);
        }
        finally {
          this.tagsHaveRecordsUntilTimeLock.readLock().unlock();
        }

        for (HistoryUpdateId historyUpdateId : dataIds) {
          final Timestamp registeredEndTime = getTagHaveRecordsUntilTime(historyUpdateId);
          if (registeredEndTime == null) {
            // If there isn't any registered time, it have not been loaded at
            // all.
            haveAllValues = false;
            break;
          }
          else if (registeredEndTime.before(leastLoadedTimestamp)) {
            // If the timestamp is less than all the other timestamps so far
            // we can only say everything is only fully loaded until this time.
            leastLoadedTimestamp = registeredEndTime;
          }
        }

        if (!haveAllValues) {
          // If we don't have all the values, something is not loaded at all
          this.recordsLoadedUntilTime = getStart();
        }
        else {
          // Everything is fully loaded until this timestamp
          this.recordsLoadedUntilTime = leastLoadedTimestamp;
        }

        if (recordsLoadedUntilTimeOldValue == null || !recordsLoadedUntilTimeOldValue.equals(this.recordsLoadedUntilTime)) {
          newTimestamp = this.recordsLoadedUntilTime;
        }

        this.recordsLoadedUntilTimeIsDirty = false;
      }
      finally {
        this.recordsLoadedUntilTimeLock.writeLock().unlock();
      }

      // Notifies the listeners if the timestamp have changed
      if (newTimestamp != null) {
        // Notifies listeners
        firePlaybackBufferIntervalUpdated(newTimestamp);

        if (isLoadingComplete(newTimestamp)) {
          // If loading is complete, notifies the listeners about it
          firePlaybackBufferFullyLoaded();
        }
      }
    }

    return newTimestamp != null;
  }

  /**
   * Is called whenever any part of history is changed
   */
  protected void historyChanged() {
    this.setRecordsLoadedUntilTimeIsDirty(true);
  }

  /**
   * Called before doing multiple changes to the records / history. MUST call
   * first with <code>true</code> for starting it, <code>false</code> to finish
   * it
   * 
   * @param batch
   *          <code>true</code> when starting a batch, <code>false</code> when
   *          finished.
   */
  public void setBatching(final boolean batch) {
    int currentlyBatchesGoingOn = 0;
    try {
      this.batchesGoingOnLock.writeLock().lock();
      if (batch) {
        this.batchesGoingOn++;
      }
      else {
        this.batchesGoingOn--;
      }
      currentlyBatchesGoingOn = this.batchesGoingOn;
    }
    finally {
      this.batchesGoingOnLock.writeLock().unlock();
    }
    if (!batch && currentlyBatchesGoingOn <= 0) {
      this.batchesIsFinished();
    }
  }

  /**
   * 
   * @return <code>true</code> if currently in a batch, <code>false</code>
   *         otherwise
   */
  public boolean isBatching() {
    try {
      this.batchesGoingOnLock.writeLock().lock();
      return this.batchesGoingOn > 0;
    }
    finally {
      this.batchesGoingOnLock.writeLock().unlock();
    }
  }

  /**
   * Invoked when all batches is finished
   */
  protected void batchesIsFinished() {
    updateTimeRecordsIsLoadedUntil();
    
    // Firing postponed "fireOnObjectsInitialized"
    List<HistoryUpdateId> initializedHistoryIds = null;
    synchronized (this.postponedFireOnObjectsInitialized) {
      if (this.postponedFireOnObjectsInitialized.size() > 0) {
        initializedHistoryIds = new ArrayList<HistoryUpdateId>();
        initializedHistoryIds.addAll(this.postponedFireOnObjectsInitialized);
        this.postponedFireOnObjectsInitialized.clear();
      }
    }
    if (initializedHistoryIds != null) {
      fireOnObjectsInitialized(initializedHistoryIds);
    }
    
    // Firing postponed "fireOnObjectsInitialized"
    List<HistoryUpdateId> changedHistoryIds = null;
    synchronized (this.postponedFireOnObjectCollectionChanged) {
      if (this.postponedFireOnObjectCollectionChanged.size() > 0) {
        changedHistoryIds = new ArrayList<HistoryUpdateId>();
        changedHistoryIds.addAll(this.postponedFireOnObjectCollectionChanged);
        this.postponedFireOnObjectCollectionChanged.clear();
        
        // Don't notify listeners for the ids which are just initialized
        if (initializedHistoryIds != null) {
          changedHistoryIds.removeAll(initializedHistoryIds);
        }
      }
    }
    if (changedHistoryIds != null && changedHistoryIds.size() > 0) {
      fireOnObjectCollectionChanged(changedHistoryIds);
    }
  }

  /**
   * Removes the value from
   * 
   * @param historyUpdateId
   *          The id of the data to remove
   */
  private void removeTagHaveRecordsUntilTime(final HistoryUpdateId historyUpdateId) {
    setTagHaveRecordsUntilTime(historyUpdateId, null);
  }

  /**
   * The value is only set if <code>time</code> is after the value already there
   * (if any)
   * 
   * @param historyUpdateId
   *          The data id to set the new time for
   * @param time
   *          The time to set, or <code>null</code> to remove
   */
  private void setTagHaveRecordsUntilTime(final HistoryUpdateId historyUpdateId, final Timestamp time) {
    Boolean add = null;
    if (time == null) {
      add = false;
    }
    else {
      final Timestamp oldTimestamp = getTagHaveRecordsUntilTime(historyUpdateId);
      add = oldTimestamp == null || time.after(oldTimestamp);
      if (!add) {
        add = null;
      }
    }

    // Don't update the value if it already exists a later time for the dataId
    if (add != null) {
      try {
        this.tagsHaveRecordsUntilTimeLock.writeLock().lock();
        if (add) {
          this.tagsHaveRecordsUntilTime.put(historyUpdateId, time);
        }
        else {
          this.tagsHaveRecordsUntilTime.remove(historyUpdateId);
        }
      }
      finally {
        this.tagsHaveRecordsUntilTimeLock.writeLock().unlock();
      }
      this.setRecordsLoadedUntilTimeIsDirty(true);
    }
  }

  /**
   * 
   * @param historyUpdateId
   *          The data id to retrieve the time for
   * @return The ending time for how far the records have been retrieved for the
   *         tag Id
   */
  public Timestamp getTagHaveRecordsUntilTime(final HistoryUpdateId historyUpdateId) {
    try {
      this.tagsHaveRecordsUntilTimeLock.readLock().lock();
      return this.tagsHaveRecordsUntilTime.get(historyUpdateId);
    }
    finally {
      this.tagsHaveRecordsUntilTimeLock.readLock().unlock();
    }
  }

  /**
   * This method does smart filtering on the real time values, the data which
   * does not need to be loaded is added to the store. The data which were not
   * added is returned.<br/>
   * <br/>
   * This function automatically registers the data which is filtered (i.e. not
   * returned).
   * 
   * @see #registerTags(HistoryUpdateId...)
   * 
   * @param currentValuesCollection
   *          A collection of all the current real time values. It is assumed
   *          that the TagValueUpdates in this collection have the most recent
   *          value
   * @return The object ids which is not added to the store, and must be retrieved
   *         from the short term log
   */
  public Collection<HistoryUpdateId> filterRealTimeValues(final TagValueUpdate... currentValuesCollection) {
    // The result which will be returned
    final Set<HistoryUpdateId> result = new HashSet<HistoryUpdateId>();

    // A list of removed data / not added to the result list
    final Set<HistoryUpdateId> registeredTags = new HashSet<HistoryUpdateId>();

    // Starting a batch
    this.setBatching(true);

    for (final TagValueUpdate tag : currentValuesCollection) {
      if (tag == null || tag.getId() == null) {
        continue;
      }

      final HistoryUpdateId tagDataId = new TagValueUpdateId(tag.getId());
      
      // If the tag doesn't existing in real time, it must be retrieved
      if (tag.getDataTagQuality() != null && !tag.getDataTagQuality().isExistingTag()) {
        result.add(tagDataId);
        continue;
      }
      
      // If the tag is already finish loading
      final Timestamp loadedUntilTime = getTagHaveRecordsUntilTime(tagDataId);
      if (loadedUntilTime != null && loadedUntilTime.compareTo(getEnd()) >= 0) {
        continue;
      }

      // If the tag from the realtime collection have a timestamp of before
      // the start time of the playback, it means it already have the latest
      // value, and don't need to look it up on the server.
      if (!registeredTags.contains(tagDataId)) {

        // If the Timestamp is from before the start time
        // it means no updates have been made in the periode to today.
        // It therefore doesn't need to be fetched from the server.
        if (tag.getServerTimestamp() != null && tag.getServerTimestamp().before(this.getStart())) {
          boolean wasRegistered = false;

          // Puts the TagHistory into the dataTagHistories map
          try {
            this.dataTagHistoriesLock.writeLock().lock();
            wasRegistered = !this.dataHistories.containsKey(tagDataId);
            if (wasRegistered) {
              final HistoryGroup historyGroup = new HistoryGroup(tagDataId);
              
              // Adds the record to the histories
              historyGroup.add(new HistoryTagValueUpdateImpl(tag));

              this.dataHistories.put(tagDataId, historyGroup);
            }
          }
          finally {
            this.dataTagHistoriesLock.writeLock().unlock();
          }

          if (wasRegistered) {
            // Adds the tag to the list of registered data as it is not added to
            // the result
            registeredTags.add(tagDataId);

            // Sets the tag to initialized
            setTagInitialized(true, tagDataId);
          }

          // This tag is now fully loaded
          this.setTagHaveRecordsUntilTime(tagDataId, getEnd());
        }
        else if (loadedUntilTime == null) {
          // Adds the tag to the result, since it must be retrieved from the
          // server
          result.add(tagDataId);
        }
      }
    } // for loop

    // Ending the batch
    this.setBatching(false);

    if (registeredTags.size() > 0) {
      this.fireOnObjectsInitialized(registeredTags);
      
      // Notifies which data have been added
      this.fireOnObjectCollectionChanged(registeredTags);
    }

    return result;
  }

  /**
   * 
   * @param historyUpdateId
   *          The tag id to get the value for
   * @param time
   *          The time of the value to retrieve
   * @return The {@link HistoryUpdate} that belongs to the specific time,
   *         <code>null</code> if there aren't one
   */
  public HistoryUpdate getTagValue(final HistoryUpdateId historyUpdateId, final long time) {
    final HistoryGroup history = this.getHistory(historyUpdateId);

    HistoryUpdate latestValue = null;
    if (history != null) {
      final HistoryUpdate[] logRecords = history.getHistory();

      // check if there is any history for this data tag, if none is found
      // we set the latest value to null which results in invalidating the
      // data tag later on.

      if (logRecords != null && logRecords.length > 0) {
        // init latest value with first log record

        for (HistoryUpdate record : logRecords) {
          // look for the first timestamp that is over the current time,
          // then take the value just before

          if (record != null && record.getExecutionTimestamp() != null) {
            // If record is null or the timestamp is null, it is useless..
            if (record.getExecutionTimestamp().getTime() > time) {
              break;
            }
            latestValue = record;
          }
        }
      }
    }
    return latestValue;
  }

  /**
   * Adds the history records to the store with this initialization data
   * 
   * @param historyUpdateIds
   *          The data ids which is loaded. The loaded end time will be set for
   *          all these data ids.
   * @param historyUpdates
   *          The initialization data
   * @return the number of records added
   */
  public int addInitialTagValueUpdates(final Collection<HistoryUpdateId> historyUpdateIds, final Collection<HistoryUpdate> historyUpdates) {
    final Set<HistoryUpdateId> initializedTags = new HashSet<HistoryUpdateId>();
    for (final HistoryUpdateId tag : historyUpdateIds) {
      initializedTags.add(tag);
    }
    setTagInitialized(true, initializedTags);
    
    // The initial data is only loaded until zero hour at the start time
    return addHistoryValues(historyUpdateIds, historyUpdates, getStart());
  }

  /**
   * Adds the history records to the store, except the data which are not
   * initialized. Call the {@link #addInitialHistoryData(Collection, Timestamp)}
   * with the initial data for the data you want to initialize.
   * 
   * @param historyUpdateIds
   *          The data ids which is loaded. The loaded end time will be set for
   *          all these data ids.
   * @param historyUpdates
   *          The collection of values to add
   * @param endTime
   *          The end time of the data in the collection
   * @return the number of records added
   */
  public int addHistoryValues(final Collection<HistoryUpdateId> historyUpdateIds, final Collection<HistoryUpdate> historyUpdates, final Timestamp endTime) {
    return addHistoryValues(historyUpdateIds,
        HistoryDataUtil.toTagHistoryCollection(historyUpdates).toArray(new HistoryGroup[0]), endTime);
  }

  /**
   * Adds the history records to the store, except the data which are not
   * initialized. Call the {@link #addInitialHistoryData(Collection, Timestamp)}
   * with the initial data for the data you want to initialize.
   * 
   * @param historyUpdateIds
   *          The data ids which is loaded. The loaded end time will be set for
   *          all these data ids.
   * @param tagHistories
   *          The collection to add
   * @param endTime
   *          The end time of the data in the collection
   * @return The number of records that is added
   * 
   */
  private int addHistoryValues(final Collection<HistoryUpdateId> historyUpdateIds, final HistoryGroup[] tagHistories, final Timestamp endTime) {

    // Starting a batch
    this.setBatching(true);

    // A list storing all the new dataIds. Is used at the end to notify listeners
    final List<HistoryUpdateId> addedIds = new ArrayList<HistoryUpdateId>();
    
    // A list of data which have their first record added
    final List<HistoryUpdateId> initializedIds = new ArrayList<HistoryUpdateId>();

    int recordsAdded = 0;

    // Loops through the tagHistoryCollection. Adds all the valid ones.
    for (final HistoryGroup newHistory : tagHistories) {
      if (newHistory.getHistory() != null && newHistory.getHistory().length > 0) {

        // The data is not added if the tag is not registered
        if (!isTagInitialized(newHistory.getTagId())) {
          continue;
        }

        addedIds.add(newHistory.getTagId());

        // Checks if a DataTagHistory already exists for the tag,
        // if not it this new history object will be added.
        try {
          this.dataTagHistoriesLock.writeLock().lock();
          HistoryGroup historyGroup = this.dataHistories.get(newHistory.getTagId());

          final HistoryUpdate[] historyValues = newHistory.getHistory();
          
          if (historyValues != null && historyValues.length > 0) {
            // If tagHistory not already exists it it created and added to the
            // dataTagHistories map
            if (historyGroup == null) {
              historyGroup = new HistoryGroup(newHistory.getTagId());
              this.dataHistories.put(historyGroup.getTagId(), historyGroup);
              initializedIds.add(historyGroup.getTagId());
              addedIds.remove(historyGroup.getTagId());
            }
  
            // Adds all the records of class TagValueUpdate which is not null
            // and does have a Timestamp.
            for (final HistoryUpdate tagValueUpdate : historyValues) {
              if (tagValueUpdate.getExecutionTimestamp() != null) {
                historyGroup.add(tagValueUpdate);
                recordsAdded++;
              }
            }
  
            // Sorts the records
            this.sortRecordsInDataTagHistory(historyGroup);
          }
        }
        finally {
          this.dataTagHistoriesLock.writeLock().unlock();
        }
      }
    }
    
    for (final HistoryUpdateId historyUpdateId : historyUpdateIds) {
      setTagHaveRecordsUntilTime(historyUpdateId, endTime);
    }
    
    // Notifies that the history is changed
    this.historyChanged();

    if (initializedIds.size() > 0) {
      this.fireOnObjectsInitialized(initializedIds);
    }
    if (addedIds.size() > 0) {
      // Notifies that the data have been added
      this.fireOnObjectCollectionChanged(addedIds);
    }
    
    // Ending the batch
    this.setBatching(false);

    return recordsAdded;
  }

  /**
   * Sorts the records by {@link TagValueUpdate#getServerTimestamp()} ascending
   * 
   * @param history
   *          The history containing the records to sort
   */
  private void sortRecordsInDataTagHistory(final HistoryGroup history) {
    history.sortHistory(new Comparator<HistoryUpdate>() {
      /**
       * Sort by {@link TagValueUpdate#getServerTimestamp()}
       */
      @Override
      public int compare(final HistoryUpdate tagValueUpdate1, final HistoryUpdate tagValueUpdate2) {
        if (tagValueUpdate1 == null || tagValueUpdate2 == null) {
          return whichIsNull(tagValueUpdate1, tagValueUpdate2);
        }

        return tagValueUpdate1.getExecutionTimestamp().compareTo(tagValueUpdate2.getExecutionTimestamp());
      }
    });
  }

  /**
   * Tells you which one of the objects is null. At least one of them MUST be
   * <code>null</code> for it to succeed
   * 
   * @param obj1
   *          HistoryUpdateId to compare
   * @param obj2
   *          HistoryUpdateId to compare
   * @return <code>0</code> (zero) if both is null, -1 if obj1 is null, 1 if
   *         obj2 is null
   */
  private int whichIsNull(final Object obj1, final Object obj2) {
    if (obj1 == obj2) {
      // Both is null
      return 0;
    }
    else if (obj1 == null) {
      return -1;
    }
    else {
      return 1;
    }
  }

  /**
   * 
   * @param id
   *          The id to search for
   * @return The {@link HistoryGroup} associated with the dataId
   */
  public HistoryGroup getHistory(final HistoryUpdateId id) {
    try {
      this.dataTagHistoriesLock.readLock().lock();
      return this.dataHistories.get(id);
    }
    finally {
      this.dataTagHistoriesLock.readLock().unlock();
    }
  }

  /**
   * 
   * @return An array of all the data ids registered
   */
  public HistoryUpdateId[] getRegisteredDataIds() {
    try {
      this.tagsHaveRecordsUntilTimeLock.readLock().lock();
      return this.tagsHaveRecordsUntilTime.keySet().toArray(new HistoryUpdateId[0]);
    }
    finally {
      this.tagsHaveRecordsUntilTimeLock.readLock().unlock();
    }
  }
  
  /**
   * @return a list of all the tag value update ids that is registered
   */
  public Collection<TagValueUpdateId> getRegisteredTagValueUpdateIds() {
    final List<TagValueUpdateId> result = new ArrayList<TagValueUpdateId>();
    for (HistoryUpdateId id : getRegisteredDataIds()) {
      if (id.isTagValueUpdateId()) {
        result.add(id.getTagValueUpdateId());
      }
    }
    return result;
  }

  /**
   * Checks if the tag have been registered ({@link #registerTags(HistoryUpdateId...)}) or
   * if it have been registered by
   * {@link #filterRealTimeValues(TagValueUpdate...)}
   * 
   * @param historyUpdateId
   *          The tag to search for
   * @return <code>true</code> if the tag is in the list of data,
   *         <code>false</code> otherwise
   */
  public boolean isRegistered(final HistoryUpdateId historyUpdateId) {
    try {
      this.tagsHaveRecordsUntilTimeLock.readLock().lock();
      return this.tagsHaveRecordsUntilTime.containsKey(historyUpdateId);
    }
    finally {
      this.tagsHaveRecordsUntilTimeLock.readLock().unlock();
    }
  }

  /**
   * @param historyUpdateId
   *          The dataId of the DataTagHistory to remove
   * @return The removed {@link HistoryGroup}, or <code>null</code> if not found
   */
  public HistoryGroup removeDataTagHistory(final HistoryUpdateId historyUpdateId) {

    // Sets the tag to not initialized
    setTagInitialized(false, historyUpdateId);

    // Removes tag from tagsHaveRecordsUntilTime
    removeTagHaveRecordsUntilTime(historyUpdateId);

    HistoryGroup removedElement = null;
    // Removes the tag from dataTagHistories
    try {
      this.dataTagHistoriesLock.writeLock().lock();
      removedElement = this.dataHistories.remove(historyUpdateId);
    }
    finally {
      this.dataTagHistoriesLock.writeLock().unlock();
    }
    // Notifies that the history is changed
    this.historyChanged();
    return removedElement;
  }

  /**
   * Removes the data, and also removes the data ids from the accepted list
   * 
   * @param historyUpdateIds
   *          The dataIds of the {@link DataTagHistory}s to remove
   */
  public void removeDataTagHistory(final Collection<HistoryUpdateId> historyUpdateIds) {
    // Starts a batch
    this.setBatching(true);

    // Removes the data
    for (HistoryUpdateId historyUpdateId : historyUpdateIds) {
      removeDataTagHistory(historyUpdateId);
    }

    // Ends the batch
    this.setBatching(false);
  }

  /*
   * HistoryStoreListener Listeners
   */

  /**
   * Fires the {@link HistoryStoreListener#onDataCollectionChanged(Collection)} on all the
   * listeners
   * 
   * @param ids
   *          The argument to send
   */
  protected void fireOnObjectCollectionChanged(final Collection<HistoryUpdateId> ids) {
    if (!postponeCall(this.postponedFireOnObjectCollectionChanged, ids)) {
      for (HistoryStoreListener listener : getHistoryStoreListeners()) {
        listener.onDataCollectionChanged(ids);
      }
    }
  }
  
  /**
   * Fires the {@link HistoryStoreListener#onDataInitialized(Collection)} on all the
   * listeners
   * 
   * @param ids
   *          The argument to send
   */
  protected void fireOnObjectsInitialized(final Collection<HistoryUpdateId> ids) {
    if (!postponeCall(this.postponedFireOnObjectsInitialized, ids)) {
      for (HistoryStoreListener listener : getHistoryStoreListeners()) {
        listener.onDataInitialized(ids);
      }
    }
  }

  /**
   * 
   * @see {@link #isBatching()}
   * 
   * @param list
   *          the list to put the ids inside, if it is currently batching
   * @param ids
   *          the ids which will be put into the list if it is currently
   *          batching
   * @return <code>true</code> if currently batching, and the <code>ids</code>
   *         is added to the <code>list</code>. <code>false</code> if currently
   *         not batching, and the <code>ids</code> is NOT added to the
   *         <code>list</code>.
   */
  private boolean postponeCall(final List<HistoryUpdateId> list, final Collection<HistoryUpdateId> ids) {
    try {
      this.batchesGoingOnLock.writeLock().lock();
      if (this.batchesGoingOn > 0) {
        synchronized (list) {
          list.addAll(ids);
        }
        return true;
      }
      return false;
    }
    finally {
      this.batchesGoingOnLock.writeLock().unlock();
    }
  }

  /*
   * Methods for adding and removing listeners
   */

  /**
   * 
   * @return All the HistoryStoreListener listeners
   */
  protected HistoryStoreListener[] getHistoryStoreListeners() {
    try {
      this.historyStoreListenersLock.readLock().lock();
      return this.historyStoreListeners.toArray(new HistoryStoreListener[0]);
    }
    finally {
      this.historyStoreListenersLock.readLock().unlock();
    }
  }

  /**
   * 
   * @param listener
   *          The listener to add
   */
  public void addHistoryStoreListener(final HistoryStoreListener listener) {
    try {
      historyStoreListenersLock.writeLock().lock();
      this.historyStoreListeners.add(listener);
    }
    finally {
      historyStoreListenersLock.writeLock().unlock();
    }
  }

  /**
   * 
   * @param listener
   *          The listener to remove
   */
  public void removeHistoryStoreListener(final HistoryStoreListener listener) {
    try {
      historyStoreListenersLock.writeLock().lock();
      this.historyStoreListeners.remove(listener);
    }
    finally {
      historyStoreListenersLock.writeLock().unlock();
    }
  }

  /*
   * Setters and getters with locks
   */

  /**
   * 
   * @return The ending time of how far in time the records have been loaded.
   */
  public Timestamp getHistoryIsLoadedUntilTime() {
    return getHistoryIsLoadedUntilTime(false);
  }

  /**
   * 
   * @param forceUpdate
   *          <code>true</code> to force calculation of the latest value. This
   *          may sometimes be required since the value is calculated by
   *          conditions of if the TagValueUpdate have listeners or not. When a
   *          listener is removed from a client data tag, there is at the moment
   *          no possible way of knowing it. <br/>
   *          Default is <code>false</code>
   * @return The ending time of how far in time the records have been loaded.
   */
  public Timestamp getHistoryIsLoadedUntilTime(final boolean forceUpdate) {
    if (forceUpdate) {
      this.setRecordsLoadedUntilTimeIsDirty(true);
    }

    // Be sure to get the latest value by updating it first
    this.updateTimeRecordsIsLoadedUntil();

    try {
      this.recordsLoadedUntilTimeLock.readLock().lock();
      return this.recordsLoadedUntilTime;
    }
    finally {
      this.recordsLoadedUntilTimeLock.readLock().unlock();
    }
  }

  /**
   * 
   * @param historyUpdateId
   *          The tag id to check
   * @return <code>true</code> if the tag have been registered
   */
  public boolean isTagRegistered(final HistoryUpdateId historyUpdateId) {
    try {
      this.tagsHaveRecordsUntilTimeLock.readLock().lock();
      return tagsHaveRecordsUntilTime.containsKey(historyUpdateId);
    }
    finally {
      this.tagsHaveRecordsUntilTimeLock.readLock().unlock();
    }
  }

  /**
   * 
   * @return <code>true</code> if all the data have been loaded until the end
   *         timestamp
   */
  public boolean isLoadingComplete() {
    return isLoadingComplete(getHistoryIsLoadedUntilTime());
  }

  /**
   * 
   * @param forceUpdate
   *          true to force calculation of the latest value. This may sometimes
   *          be required since the value is calculated by conditions of if the
   *          TagValueUpdate have listeners or not. When a listener is removed
   *          from a client data tag, there is at the moment no possible way of
   *          knowing it. Default is false
   * 
   * @return <code>true</code> if all the data have been loaded until the end
   *         timestamp
   */
  public boolean isLoadingComplete(final boolean forceUpdate) {
    return isLoadingComplete(getHistoryIsLoadedUntilTime(forceUpdate));
  }

  /**
   * 
   * @param haveLoadedUntil
   *          The time that have been loaded until
   * @return <code>true</code> if it have been loaded until the end timestamp.
   */
  private boolean isLoadingComplete(final Timestamp haveLoadedUntil) {
    return !haveLoadedUntil.before(this.getEnd());
  }

  /**
   * Changes the <code>recordsLoadedUntilTimeIsDirty</code> to the given value
   * 
   * @param dirty
   *          The value to put
   */
  protected void setRecordsLoadedUntilTimeIsDirty(final boolean dirty) {
    try {
      this.recordsLoadedUntilTimeLock.writeLock().lock();
      this.recordsLoadedUntilTimeIsDirty = dirty;
    }
    finally {
      this.recordsLoadedUntilTimeLock.writeLock().unlock();
    }
    if (dirty && !isBatching()) {
      this.updateTimeRecordsIsLoadedUntil();
    }
  }

  /**
   * Initializes the data, by calling this with the data ids before starting the
   * actual loading, you ensure that the {@link #getHistoryIsLoadedUntilTime()}
   * returns the correct value.<br/>
   * Does not initialize the tag!
   * 
   * @param ids
   *          The data ids to add
   *          
   * @see #addInitialHistoryData(Collection)
   */
  public void registerTags(final HistoryUpdateId... ids) {
    this.setBatching(true);
    try {
      // Initializes the data
      this.tagsHaveRecordsUntilTimeLock.writeLock().lock();
      try {
        for (HistoryUpdateId historyUpdateId : ids) {
          if (HistoryStore.isTagIdAcceptable(historyUpdateId)) {
            if (this.tagsHaveRecordsUntilTime.get(historyUpdateId) == null) {
              this.tagsHaveRecordsUntilTime.put(historyUpdateId, getStart());
            }
          }
        }
      }
      finally {
        this.tagsHaveRecordsUntilTimeLock.writeLock().unlock();
      }

      // Notifies that the history is changed
      this.historyChanged();
    }
    finally {
      this.setBatching(false);
    }
  }
  
  /**
   * Unregisters the data by removing all data about the data
   * 
   * @param historyUpdateIds
   *          The id's of the data to remove
   */
  public void unregisterTags(final Collection<HistoryUpdateId> historyUpdateIds) {
    if (historyUpdateIds == null || historyUpdateIds.size() == 0) {
      return;
    }
    
    this.setBatching(true);
    try {
      // Removes the data from dataTagHistories
      try {
        this.dataTagHistoriesLock.writeLock().lock();
        for (HistoryUpdateId historyUpdateId : historyUpdateIds) {
          this.dataHistories.remove(historyUpdateId);
        }
      }
      finally {
        this.dataTagHistoriesLock.writeLock().unlock();
      }

      try {
        this.initializedTagsLock.writeLock().lock();
        this.initializedTags.removeAll(historyUpdateIds);
        
        // Removes the data from the tagsHaveRecordsUntilTime
        try {
          this.tagsHaveRecordsUntilTimeLock.writeLock().lock();
          for (HistoryUpdateId historyUpdateId : historyUpdateIds) {
            this.tagsHaveRecordsUntilTime.remove(historyUpdateId);
          }
        }
        finally {
          this.tagsHaveRecordsUntilTimeLock.writeLock().unlock();
        }
        
      }
      finally {
        this.initializedTagsLock.writeLock().unlock();
      }
      
      // Notifies that the history is changed
      this.historyChanged();
    }
    finally {
      this.setBatching(false);
    }
  }

  /**
   * 
   * @param id
   *          The id to check
   * @return <code>true</code> if the object have been initialized, i.e. the first
   *         value have been loaded
   */
  public boolean isTagInitialized(final HistoryUpdateId id) {
    this.initializedTagsLock.readLock().lock();
    try {
      return this.initializedTags.contains(id);
    }
    finally {
      this.initializedTagsLock.readLock().unlock();
    }
  }

  /**
   * 
   * @return <code>true</code> if there exist uninitialized data
   */
  public boolean isUninitializedTags() {
    for (final HistoryUpdateId historyUpdateId : this.getRegisteredDataIds()) {
      if (!isTagInitialized(historyUpdateId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 
   * @return The data which are not yet initialized
   */
  public Collection<HistoryUpdateId> getUninitializedTags() {
    final Set<HistoryUpdateId> result = new HashSet<HistoryUpdateId>();

    for (final HistoryUpdateId historyUpdateId : this.getRegisteredDataIds()) {
      if (!isTagInitialized(historyUpdateId)) {
        result.add(historyUpdateId);
      }
    }

    return result;
  }

  /**
   * 
   * @param initialized
   *          <code>true</code> to set it to initialized, <code>false</code> to
   *          set it to not initialized
   * @param dataIds
   *          The data ids to change the initialization state for
   */
  private void setTagInitialized(final boolean initialized, final HistoryUpdateId ... dataIds) {
    setTagInitialized(initialized, Arrays.asList(dataIds));
  }

  /**
   * 
   * @param initialized
   *          <code>true</code> to set it to initialized, <code>false</code> to
   *          set it to not initialized
   * @param historyUpdateIds
   *          The data ids to change the initialization state for
   */
  private void setTagInitialized(final boolean initialized, final Collection<HistoryUpdateId> historyUpdateIds) {
    final List<HistoryUpdateId> filteredDataIds;
    
    if (initialized) {
      filteredDataIds = new ArrayList<HistoryUpdateId>(historyUpdateIds.size());
      for (HistoryUpdateId historyUpdateId : historyUpdateIds) {
        if (HistoryStore.isTagIdAcceptable(historyUpdateId)) {
          filteredDataIds.add(historyUpdateId);
        }
      }
    }
    else {
      filteredDataIds = null;
    }
    
    this.initializedTagsLock.writeLock().lock();
    try {
      if (initialized) {
        this.initializedTags.addAll(filteredDataIds);
      }
      else {
        this.initializedTags.removeAll(historyUpdateIds);
      }
    }
    finally {
      this.initializedTagsLock.writeLock().unlock();
    }
  }

  /**
   * 
   * @param id
   * @return <code>true</code> if the tag id is acceptable, <code>false</code>
   *         if it is isn't i.e. it is <code>-1</code> or <code>0</code>
   */
  private static boolean isTagIdAcceptable(final HistoryUpdateId historyUpdateId) {
    for (final HistoryUpdateId illegalId : ILLEGAL_IDS) {
      if (historyUpdateId.equals(illegalId)) {
        return false;
      }
    }
    return true;
  }
  
  /*
   * HistoryStoreListener invoker
   */

  /**
   * Notifies the listeners about which time has been loaded to
   */
  public void firePlaybackBufferIntervalUpdated() {
    firePlaybackBufferIntervalUpdated(getHistoryIsLoadedUntilTime());
  }

  /**
   * Notifies the listeners about the playback how far have been loaded
   * 
   * @param loadedUntil
   *          The time that have been loaded until
   */
  private void firePlaybackBufferIntervalUpdated(final Timestamp loadedUntil) {
    for (HistoryStoreListener listener : getHistoryStoreListeners()) {
      listener.onPlaybackBufferIntervalUpdated(loadedUntil);
    }
  }

  /**
   * Notifies the listeners that the playback is fully loaded
   */
  private void firePlaybackBufferFullyLoaded() {
    for (HistoryStoreListener listener : getHistoryStoreListeners()) {
      listener.onPlaybackBufferFullyLoaded();
    }
  }

  /*
   * Setters and getters
   */

  /**
   * @return the start
   */
  public Timestamp getStart() {
    return start;
  }

  /**
   * @param start
   *          the start to set
   */
  public void setStart(final Timestamp start) {
    this.start = start;
  }

  /**
   * @return the end
   */
  public Timestamp getEnd() {
    return end;
  }

  /**
   * @param end
   *          the end to set
   */
  public void setEnd(Timestamp end) {
    this.end = end;
  }

}
