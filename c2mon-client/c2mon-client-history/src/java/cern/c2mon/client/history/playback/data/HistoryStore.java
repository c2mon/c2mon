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

import cern.c2mon.client.history.playback.data.event.HistoryStoreListener;
import cern.c2mon.client.history.playback.data.utilities.HistoryDataUtil;
import cern.c2mon.client.history.util.TagHistory;
import cern.c2mon.shared.client.tag.TagValueUpdate;

/**
 * This class holds all history data which is loaded by {@link HistoryLoader}
 * 
 * @see HistoryStoreListener
 * 
 * @author vdeila
 */
public class HistoryStore {

  /** A list of tag ids which will be filtered out */
  private static final Long[] ILLEGAL_TAG_IDS = new Long[] { -1L, 0L };
  
  /** A map containing all history for all the objects */
  private Map<Long, TagHistory> dataTagHistories;

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
   * A map with the tagIds, each containing the ending time for how far the
   * records have been retrieved. All tags which can be loaded will be in this
   * list, even just with the start time as timestamp.
   */
  private Map<Long, Timestamp> tagsHaveRecordsUntilTime;

  /** A lock for tagsHaveRecordsUntilTime list */
  private ReentrantReadWriteLock tagsHaveRecordsUntilTimeLock;

  /** 
   * Tags that have been initialized, i.e. first value is loaded.
   * A tag will not be loaded before it is initialized.
   */
  private Set<Long> initializedTags;

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

  /**
   * Constructor
   */
  public HistoryStore() {
    this.dataTagHistoriesLock = new ReentrantReadWriteLock();
    this.historyStoreListenersLock = new ReentrantReadWriteLock();
    this.tagsHaveRecordsUntilTimeLock = new ReentrantReadWriteLock();
    this.initializedTagsLock = new ReentrantReadWriteLock();
    this.recordsLoadedUntilTimeLock = new ReentrantReadWriteLock();
    this.batchesGoingOnLock = new ReentrantReadWriteLock();

    final long theTime = System.currentTimeMillis();
    this.start = new Timestamp(theTime);
    this.end = new Timestamp(theTime);

    this.dataTagHistories = new Hashtable<Long, TagHistory>();
    this.historyStoreListeners = new ArrayList<HistoryStoreListener>();
    this.tagsHaveRecordsUntilTime = new HashMap<Long, Timestamp>();
    this.initializedTags = new HashSet<Long>();
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
      this.dataTagHistories.clear();
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
   *         otherwise. Does only compare tags that is "accepted"!
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
        final Long[] tagIds;
        try {
          this.tagsHaveRecordsUntilTimeLock.readLock().lock();
          tagIds = this.tagsHaveRecordsUntilTime.keySet().toArray(new Long[0]);
        }
        finally {
          this.tagsHaveRecordsUntilTimeLock.readLock().unlock();
        }

        for (Long tagId : tagIds) {
          final Timestamp registeredEndTime = getTagHaveRecordsUntilTime(tagId);
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
          this.recordsLoadedUntilTime = this.getStart();
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
  }

  /**
   * Removes the value from
   * 
   * @param tagId
   *          The id of the tag to remove
   */
  private void removeTagHaveRecordsUntilTime(final Long tagId) {
    setTagHaveRecordsUntilTime(tagId, null);
  }

  /**
   * The value is only set if <code>time</code> is after the value already there
   * (if any)
   * 
   * @param tagId
   *          The tag Id to set the new time for
   * @param time
   *          The time to set, or <code>null</code> to remove
   */
  private void setTagHaveRecordsUntilTime(final Long tagId, final Timestamp time) {
    Boolean add = null;
    if (time == null) {
      add = false;
    }
    else {
      final Timestamp oldTimestamp = getTagHaveRecordsUntilTime(tagId);
      add = oldTimestamp == null || time.after(oldTimestamp);
      if (!add) {
        add = null;
      }
    }

    // Don't update the value if it already exists a later time for the tagId
    if (add != null) {
      try {
        this.tagsHaveRecordsUntilTimeLock.writeLock().lock();
        if (add) {
          this.tagsHaveRecordsUntilTime.put(tagId, time);
        }
        else {
          this.tagsHaveRecordsUntilTime.remove(tagId);
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
   * @param tagId
   *          The tag id to retrieve the time for
   * @return The ending time for how far the records have been retrieved for the
   *         tag Id
   */
  public Timestamp getTagHaveRecordsUntilTime(final Long tagId) {
    try {
      this.tagsHaveRecordsUntilTimeLock.readLock().lock();
      return this.tagsHaveRecordsUntilTime.get(tagId);
    }
    finally {
      this.tagsHaveRecordsUntilTimeLock.readLock().unlock();
    }
  }

  /**
   * This method does smart filtering on the real time values, the tags which
   * does not need to be loaded is added to the store. The tags which were not
   * added is returned.<br/>
   * <br/>
   * This function automatically registers the tags which is filtered (i.e. not
   * returned).
   * 
   * @see #registerTags(Long...)
   * 
   * @param currentValuesCollection
   *          A collection of all the current real time values. It is assumed
   *          that the TagValueUpdates in this collection have the most recent
   *          value
   * @return The tagIds which is not added to the store, and must be retrieved
   *         from the short term log
   */
  public Collection<Long> filterRealTimeValues(final TagValueUpdate... currentValuesCollection) {
    // The result which will be returned
    final Set<Long> result = new HashSet<Long>();

    // A list of removed tags / not added to the result list
    final Set<Long> registeredTags = new HashSet<Long>();

    // Starting a batch
    this.setBatching(true);

    for (final TagValueUpdate tag : currentValuesCollection) {
      if (tag == null || tag.getId() == null) {
        continue;
      }

      // If the tag doesn't existing in real time, it must be retrieved
      if (tag.getDataTagQuality() != null && !tag.getDataTagQuality().isExistingTag()) {
        result.add(tag.getId());
        continue;
      }

      // If the tag is already finish loading
      final Timestamp loadedUntilTime = getTagHaveRecordsUntilTime(tag.getId());
      if (loadedUntilTime != null && loadedUntilTime.compareTo(getEnd()) >= 0) {
        continue;
      }

      // If the tag from the realtime collection have a timestamp of before
      // the start time of the playback, it means it already have the latest
      // value, and don't need to look it up on the server.
      if (!registeredTags.contains(tag.getId())) {

        // If the Timestamp is from before the start time
        // it means no updates have been made in the periode to today.
        // It therefore doesn't need to be fetched from the server.
        if (tag.getServerTimestamp() != null && tag.getServerTimestamp().before(this.getStart())) {
          boolean wasRegistered = false;

          // Puts the TagHistory into the dataTagHistories map
          try {
            this.dataTagHistoriesLock.writeLock().lock();
            wasRegistered = !this.dataTagHistories.containsKey(tag.getId());
            if (wasRegistered) {
              final TagHistory tagHistory = new TagHistory(tag.getId());

              // Adds the record to the histories
              tagHistory.add(tag);

              this.dataTagHistories.put(tagHistory.getTagId(), tagHistory);
            }
          }
          finally {
            this.dataTagHistoriesLock.writeLock().unlock();
          }

          if (wasRegistered) {
            // Adds the tag to the list of registered tags as it is not added to
            // the result
            registeredTags.add(tag.getId());

            // Sets the tag to initialized
            setTagInitialized(true, tag.getId());
          }

          // This tag is now fully loaded
          this.setTagHaveRecordsUntilTime(tag.getId(), getEnd());
        }
        else if (loadedUntilTime == null) {
          // Adds the tag to the result, since it must be retrieved from the
          // server
          result.add(tag.getId());
        }
      }
    } // for loop

    // Ending the batch
    this.setBatching(false);

    if (registeredTags.size() > 0) {
      this.fireOnTagInitialized(registeredTags);
      
      // Notifies which tags have been added
      this.fireTagsAdded(registeredTags);
    }

    return result;
  }

  /**
   * 
   * @param tagId
   *          The tag id to get the value for
   * @param time
   *          The time of the value to retrieve
   * @return The {@link TagValueUpdate} that belongs to the specific time,
   *         <code>null</code> if there aren't one
   */
  public TagValueUpdate getTagValue(final long tagId, final long time) {
    final TagHistory history = this.getTagHistory(tagId);

    TagValueUpdate latestValue = null;
    if (history != null) {
      final TagValueUpdate[] logRecords = history.getHistory();

      // check if there is any history for this data tag, if none is found
      // we set the latest value to null which results in invalidating the
      // data tag later on.

      if (logRecords != null && logRecords.length > 0) {
        // init latest value with first log record

        for (TagValueUpdate record : logRecords) {
          // look for the first timestamp that is over the current time,
          // then take the value just before

          if (record != null && record.getServerTimestamp() != null) {
            // If record is null or the timestamp is null, it is useless..
            if (record.getServerTimestamp().getTime() > time) {
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
   * @param tagIds
   *          The tag ids which is loaded. The loaded end time will be set for
   *          all these tag ids.
   * @param tagValueUpdates
   *          The initialization data
   */
  public int addInitialHistoryData(final Collection<Long> tagIds, final Collection<TagValueUpdate> tagValueUpdates) {
    final Set<Long> initializedTags = new HashSet<Long>();
    for (final Long tag : tagIds) {
      initializedTags.add(tag);
    }
    setTagInitialized(true, initializedTags);
    return addHistoryValues(tagIds, tagValueUpdates, getStart());
  }

  /**
   * Adds the history records to the store, except the tags which are not
   * initialized. Call the {@link #addInitialHistoryData(Collection, Timestamp)}
   * with the initial data for the tags you want to initialize.
   * 
   * @param tagIds
   *          The tag ids which is loaded. The loaded end time will be set for
   *          all these tag ids.
   * @param tagValueUpdates
   *          The collection of values to add
   * @param endTime
   *          The end time of the tags in the collection
   * 
   */
  public int addHistoryValues(final Collection<Long> tagIds, final Collection<TagValueUpdate> tagValueUpdates, final Timestamp endTime) {
    
    return addHistoryValues(tagIds,
        HistoryDataUtil.toTagHistoryCollection(tagValueUpdates).toArray(new TagHistory[0]), endTime);
  }

  /**
   * Adds the history records to the store, except the tags which are not
   * initialized. Call the {@link #addInitialHistoryData(Collection, Timestamp)}
   * with the initial data for the tags you want to initialize.
   * 
   * @param tagIds
   *          The tag ids which is loaded. The loaded end time will be set for
   *          all these tag ids.
   * @param tagHistories
   *          The collection to add
   * @param endTime
   *          The end time of the tags in the collection
   * @return The number of records that is added
   * 
   */
  private int addHistoryValues(final Collection<Long> tagIds, final TagHistory[] tagHistories, final Timestamp endTime) {

    // Starting a batch
    this.setBatching(true);

    // A list storing all the new tagIds. Is used at the end to notify listeners
    final List<Long> addedTagIds = new ArrayList<Long>();
    
    // A list of tags which have their first record added
    final List<Long> initializedTags = new ArrayList<Long>();

    int recordsAdded = 0;

    // Loops through the tagHistoryCollection. Adds all the valid ones.
    for (final TagHistory history : tagHistories) {
      if (history.getHistory() != null && history.getHistory().length > 0) {

        // The data is not added if the tag is not registered
        if (!isTagInitialized(history.getTagId())) {
          continue;
        }

        addedTagIds.add(history.getTagId());

        // Checks if a DataTagHistory already exists for the tag,
        // if not it this new history object will be added.
        try {
          this.dataTagHistoriesLock.writeLock().lock();
          TagHistory tagHistory = this.dataTagHistories.get(history.getTagId());

          final TagValueUpdate[] historyValues = history.getHistory();
          
          if (historyValues != null && historyValues.length > 0) {
            // If tagHistory not already exists it it created and added to the
            // dataTagHistories map
            if (tagHistory == null) {
              tagHistory = new TagHistory(history.getTagId());
              this.dataTagHistories.put(tagHistory.getTagId(), tagHistory);
              initializedTags.add(tagHistory.getTagId());
            }
  
            // Adds all the records of class TagValueUpdate which is not null
            // and does have a Timestamp.
            for (final TagValueUpdate tagValueUpdate : historyValues) {
              if (tagValueUpdate.getServerTimestamp() != null) {
                tagHistory.add(tagValueUpdate);
                recordsAdded++;
              }
            }
  
            // Sorts the records
            this.sortRecordsInDataTagHistory(tagHistory);
          }
        }
        finally {
          this.dataTagHistoriesLock.writeLock().unlock();
        }
      }
    }
    
    for (final Long tagId : tagIds) {
      setTagHaveRecordsUntilTime(tagId, endTime);
    }
    
    if (initializedTags.size() > 0) {
      this.fireOnTagInitialized(initializedTags);
    }
    
    // Notifies that the tags have been added
    if (addedTagIds.size() > 0) {
      this.fireTagsAdded(addedTagIds);
    }

    // Notifies that the history is changed
    this.historyChanged();

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
  private void sortRecordsInDataTagHistory(final TagHistory history) {
    history.sortHistory(new Comparator<TagValueUpdate>() {
      /**
       * Sort by {@link TagValueUpdate#getServerTimestamp()}
       */
      @Override
      public int compare(final TagValueUpdate tagValueUpdate1, final TagValueUpdate tagValueUpdate2) {
        if (tagValueUpdate1 == null || tagValueUpdate2 == null) {
          return whichIsNull(tagValueUpdate1, tagValueUpdate2);
        }

        return tagValueUpdate1.getServerTimestamp().compareTo(tagValueUpdate2.getServerTimestamp());
      }
    });
  }

  /**
   * Tells you which one of the objects is null. At least one of them MUST be
   * <code>null</code> for it to succeed
   * 
   * @param obj1
   *          Object to compare
   * @param obj2
   *          Object to compare
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
   * @param tagId
   *          The tag id to search for
   * @return The {@link TagHistory} associated with the tagId
   */
  public TagHistory getTagHistory(final long tagId) {
    try {
      this.dataTagHistoriesLock.readLock().lock();
      return this.dataTagHistories.get(tagId);
    }
    finally {
      this.dataTagHistoriesLock.readLock().unlock();
    }
  }

  /**
   * 
   * @return An array of all the data tag ids registered
   */
  public Long[] getRegisteredTags() {
    try {
      this.tagsHaveRecordsUntilTimeLock.readLock().lock();
      return this.tagsHaveRecordsUntilTime.keySet().toArray(new Long[0]);
    }
    finally {
      this.tagsHaveRecordsUntilTimeLock.readLock().unlock();
    }
  }

  /**
   * Checks if the tag have been registered ({@link #registerTags(Long...)}) or
   * if it have been registered by
   * {@link #filterRealTimeValues(TagValueUpdate...)}
   * 
   * @param tagId
   *          The tag to search for
   * @return <code>true</code> if the tag is in the list of tags,
   *         <code>false</code> otherwise
   */
  public boolean isRegistered(final Long tagId) {
    try {
      this.tagsHaveRecordsUntilTimeLock.readLock().lock();
      return this.tagsHaveRecordsUntilTime.containsKey(tagId);
    }
    finally {
      this.tagsHaveRecordsUntilTimeLock.readLock().unlock();
    }
  }

  /**
   * @param tagId
   *          The tagId of the DataTagHistory to remove
   * @return The removed {@link TagHistory}, or <code>null</code> if not found
   */
  public TagHistory removeDataTagHistory(final Long tagId) {

    // Sets the tag to not initialized
    setTagInitialized(false, tagId);

    // Removes tag from tagsHaveRecordsUntilTime
    removeTagHaveRecordsUntilTime(tagId);

    TagHistory removedElement = null;
    // Removes the tag from dataTagHistories
    try {
      this.dataTagHistoriesLock.writeLock().lock();
      removedElement = this.dataTagHistories.remove(tagId);
    }
    finally {
      this.dataTagHistoriesLock.writeLock().unlock();
    }
    // Notifies that the history is changed
    this.historyChanged();
    return removedElement;
  }

  /**
   * Removes the tags, and also removes the tag ids from the accepted list
   * 
   * @param tagIds
   *          The tagIds of the {@link DataTagHistory}s to remove
   */
  public void removeDataTagHistory(final Collection<Long> tagIds) {
    // Starts a batch
    this.setBatching(true);

    // Removes the tags
    for (Long tagId : tagIds) {
      removeDataTagHistory(tagId);
    }

    // Ends the batch
    this.setBatching(false);
  }

  /*
   * HistoryStoreListener Listeners
   */

  /**
   * Fires the {@link HistoryStoreListener#onTagCollectionChanged(Collection)} on all the
   * listeners
   * 
   * @param tagIds
   *          The argument to send
   */
  protected void fireTagsAdded(final Collection<Long> tagIds) {
    for (HistoryStoreListener listener : getHistoryStoreListeners()) {
      listener.onTagCollectionChanged(tagIds);
    }
  }
  
  /**
   * Fires the {@link HistoryStoreListener#onTagsInitialized(Collection)} on all the
   * listeners
   * 
   * @param tagIds
   *          The argument to send
   */
  protected void fireOnTagInitialized(final Collection<Long> tagIds) {
    for (HistoryStoreListener listener : getHistoryStoreListeners()) {
      listener.onTagsInitialized(tagIds);
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
   * @param tagId
   *          The tag id to check
   * @return <code>true</code> if the tag have been registered
   */
  public boolean isTagRegistered(final Long tagId) {
    try {
      this.tagsHaveRecordsUntilTimeLock.readLock().lock();
      return tagsHaveRecordsUntilTime.containsKey(tagId);
    }
    finally {
      this.tagsHaveRecordsUntilTimeLock.readLock().unlock();
    }
  }

  /**
   * 
   * @return <code>true</code> if all the tags have been loaded until the end
   *         timestamp
   */
  public boolean isLoadingComplete() {
    return isLoadingComplete(getHistoryIsLoadedUntilTime());
  }

  /**
   * 
   * @param true to force calculation of the latest value. This may sometimes be
   *        required since the value is calculated by conditions of if the
   *        TagValueUpdate have listeners or not. When a listener is removed
   *        from a client data tag, there is at the moment no possible way of
   *        knowing it. Default is false
   * 
   * @return <code>true</code> if all the tags have been loaded until the end
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
   * Initializes the tags, by calling this with the tag ids before starting the
   * actual loading, you ensure that the {@link #getHistoryIsLoadedUntilTime()}
   * returns the correct value.<br/>
   * Does not initialize the tag!
   * 
   * @param tagIds
   *          The tag ids to add
   *          
   * @see #addInitialHistoryData(Collection)
   */
  public void registerTags(final Long... tagIds) {
    this.setBatching(true);
    try {
      // Initializes the tags
      this.tagsHaveRecordsUntilTimeLock.writeLock().lock();
      try {
        for (Long tagId : tagIds) {
          if (HistoryStore.isTagIdAcceptable(tagId)) {
            if (this.tagsHaveRecordsUntilTime.get(tagId) == null) {
              this.tagsHaveRecordsUntilTime.put(tagId, getStart());
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
   * Unregisters the tags by removing all data about the tags
   * 
   * @param tagIds
   *          The id's of the tags to remove
   */
  public void unregisterTags(final Collection<Long> tagIds) {
    this.setBatching(true);
    try {
      // Removes the tags from dataTagHistories
      try {
        this.dataTagHistoriesLock.writeLock().lock();
        for (Long tagId : tagIds) {
          this.dataTagHistories.remove(tagId);
        }
      }
      finally {
        this.dataTagHistoriesLock.writeLock().unlock();
      }

      // Removes the tags from the tagsHaveRecordsUntilTime
      try {
        this.tagsHaveRecordsUntilTimeLock.writeLock().lock();
        for (Long tagId : tagIds) {
          this.tagsHaveRecordsUntilTime.remove(tagId);
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
   * 
   * @param tagId
   *          The tag id to check
   * @return <code>true</code> if the tag have been initialized, i.e. the first
   *         value have been loaded
   */
  public boolean isTagInitialized(final Long tagId) {
    this.initializedTagsLock.readLock().lock();
    try {
      return this.initializedTags.contains(tagId);
    }
    finally {
      this.initializedTagsLock.readLock().unlock();
    }
  }

  /**
   * 
   * @return <code>true</code> if there exist uninitialized tags
   */
  public boolean isUninitializedTags() {
    for (final Long tagId : this.getRegisteredTags()) {
      if (!isTagInitialized(tagId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 
   * @return The tags which are not yet initialized
   */
  public Collection<Long> getUninitializedTags() {
    final Set<Long> result = new HashSet<Long>();

    for (final Long tagId : this.getRegisteredTags()) {
      if (!isTagInitialized(tagId)) {
        result.add(tagId);
      }
    }

    return result;
  }

  /**
   * 
   * @param initialized
   *          <code>true</code> to set it to initialized, <code>false</code> to
   *          set it to not initialized
   * @param tagIds
   *          The tag ids to change the initialization state for
   */
  private void setTagInitialized(final boolean initialized, final Long... tagIds) {
    setTagInitialized(initialized, Arrays.asList(tagIds));
  }

  /**
   * 
   * @param initialized
   *          <code>true</code> to set it to initialized, <code>false</code> to
   *          set it to not initialized
   * @param tagIds
   *          The tag ids to change the initialization state for
   */
  private void setTagInitialized(final boolean initialized, final Collection<Long> tagIds) {
    final List<Long> filteredTagIds;
    
    if (initialized) {
      filteredTagIds = new ArrayList<Long>(tagIds.size());
      for (Long tagId : tagIds) {
        if (HistoryStore.isTagIdAcceptable(tagId)) {
          filteredTagIds.add(tagId);
        }
      }
    }
    else {
      filteredTagIds = null;
    }
    
    this.initializedTagsLock.writeLock().lock();
    try {
      if (initialized) {
        this.initializedTags.addAll(filteredTagIds);
      }
      else {
        this.initializedTags.removeAll(tagIds);
      }
    }
    finally {
      this.initializedTagsLock.writeLock().unlock();
    }
  }

  /**
   * 
   * @param tagId
   * @return <code>true</code> if the tag id is acceptable, <code>false</code>
   *         if it is isn't i.e. it is <code>-1</code> or <code>0</code>
   */
  private static boolean isTagIdAcceptable(final Long tagId) {
    for (final Long illegalId : ILLEGAL_TAG_IDS) {
      if (tagId.equals(illegalId)) {
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
