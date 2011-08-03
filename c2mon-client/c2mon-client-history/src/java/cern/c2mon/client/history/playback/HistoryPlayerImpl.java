/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2010 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.client.history.playback;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.history.HistoryPlayer;
import cern.c2mon.client.common.history.HistoryPlayerEvents;
import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.PlaybackControl;
import cern.c2mon.client.common.history.Timespan;
import cern.c2mon.client.common.history.event.HistoryPlayerListener;
import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.history.playback.components.Clock;
import cern.c2mon.client.history.playback.components.ListenersManager;
import cern.c2mon.client.history.playback.data.HistoryLoader;
import cern.c2mon.client.history.playback.data.HistoryStore;
import cern.c2mon.client.history.playback.data.event.HistoryLoaderAdapter;
import cern.c2mon.client.history.playback.data.event.HistoryStoreAdapter;
import cern.c2mon.client.history.playback.exceptions.NoHistoryProviderAvailableException;
import cern.c2mon.client.history.playback.player.PlaybackControlImpl;
import cern.c2mon.client.history.playback.publish.HistoryPublisher;
import cern.c2mon.client.history.playback.publish.SupervisionListenersManager;
import cern.c2mon.client.history.playback.schedule.ClockSynchronizer;
import cern.c2mon.client.history.playback.schedule.HistoryScheduler;
import cern.c2mon.client.history.tag.HistoryTagValueUpdateImpl;
import cern.c2mon.client.jms.SupervisionListener;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.shared.common.datatag.TagQualityStatus;

/**
 * This class implements a type of video player which provides functionality to
 * playback data tag history. The data tag history is fetched from the
 * short-term log database.
 * 
 * @see HistoryPlayer
 * @see HistoryPlayerCoreAccess
 * 
 * @see HistoryScheduler
 * @see PlaybackControl
 * @see PlaybackSynchronizeControl
 * @see ClockSynchronizer
 * @see HistoryConfiguration
 * 
 * @see Clock
 * @see HistoryLoader
 * @see HistoryStore
 * 
 * @author Michael Berberich
 * @author vdeila
 */
public class HistoryPlayerImpl 
    implements HistoryPlayer, HistoryPlayerInternal, HistoryPlayerCoreAccess, HistoryPlayerEvents {

  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(HistoryPlayerImpl.class);
  
  /** A manager which keeps track of the listeners */
  private final ListenersManager<HistoryPlayerListener> historyPlayerListeners;
  
  /** Keeps the clock synchronized */
  private ClockSynchronizer clockSynchronizer;
  
  /**
   * {@link HistoryLoader} is responsible for loading all history data and put
   * it into a HistoryStore
   */
  private HistoryLoader historyLoader;
  
  /** <code>true</code> if currently in history mode */
  private boolean historyModeActive;
  
  /** The class which controls the history player */
  private final PlaybackControlImpl playbackControl;
  
  /** Manages the listeners of the tags */
  private final HistoryPublisher publisher;
  
  /** List of values which will be loaded when {@link #beginLoading()} is called */
  private final ArrayList<TagValueUpdate> tagsToRegisterLiveValue;
  
  /** Lock for {@link #tagsToRegisterLiveValue} */
  private final ReentrantLock tagsToRegisterLiveValueLock;
  
  /** For scheduling the tag value updates. */
  private final HistoryScheduler historyScheduler;
  
  /**
   * Forwards events from the HistoryLoaderListener, HistoryStoreListener and
   * HistoryProviderListener to the HistoryPlayerListeners
   */
  private final HistoryPlayerEventsForwarder eventsForwarder;
  
  /**
   * Empty constructor
   */
  public HistoryPlayerImpl() {
    this.historyPlayerListeners = new ListenersManager<HistoryPlayerListener>();
    this.tagsToRegisterLiveValue = new ArrayList<TagValueUpdate>();
    this.tagsToRegisterLiveValueLock = new ReentrantLock();
    this.historyModeActive = false;
    
    this.historyLoader = new HistoryLoader();
    this.publisher = new HistoryPublisher();
    this.playbackControl = new PlaybackControlImpl();
    this.clockSynchronizer = new ClockSynchronizer(this.playbackControl);
    this.historyScheduler = new HistoryScheduler(this);
    
    this.eventsForwarder = new HistoryPlayerEventsForwarder(this.historyPlayerListeners);
    this.historyLoader.addHistoryLoaderListener(this.eventsForwarder);
    this.historyLoader.getHistoryStore().addHistoryStoreListener(this.eventsForwarder);
    
    // Installs listeners
    installHistoryListeners();
  }
  
  /**
   * Installs the listeners for the {@link HistoryLoader} and the {@link HistoryStore}
   */
  private void installHistoryListeners() {
    // Adds a listener for when the time of history available is changed 
    this.historyLoader.getHistoryStore().addHistoryStoreListener(new HistoryStoreAdapter() {
      @Override
      public void onPlaybackBufferIntervalUpdated(final Timestamp newEndTime) {
        HistoryPlayerImpl.this.playbackBufferIntervalUpdated(newEndTime);
      }

      @Override
      public void onTagsAdded(final Collection<Long> tagIds) {
        HistoryPlayerImpl.this.historyLoaded(tagIds);
      }

      @Override
      public void onPlaybackBufferFullyLoaded() {
        HistoryPlayerImpl.this.playbackBufferFullyLoaded();
      }
    });
    
    this.historyLoader.addHistoryLoaderListener(new HistoryLoaderAdapter() {
      @Override
      public void onInitialValuesLoaded(final Collection<Long> tags) {
        // initialize the new data tags with the value at current time
        getHistoryScheduler().updateDataTagsWithValueAtCurrentTime(tags);
      }
    });
  }
  
  /**
   * Stops the history player, stops all buffering and clears all loaded data.
   */
  @Override
  public synchronized void deactivateHistoryPlayer() {
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Deactivating history mode");
    }
    
    this.historyModeActive = false;
    
    this.getPlaybackControl().pause();
    
    // Clears all the listeners
    this.publisher.clearAll();
    
    this.historyLoader.stopLoading();
    this.historyLoader.getHistoryStore().clear();
    this.historyScheduler.cancelAllScheduledEvents();
    
    this.fireDeactivatingHistoryPlayer();
  }
  
  /**
   * Configures the player with a data source and a time period
   * 
   * @param provider
   *          the provider to use to get history data
   * @param timespan
   *          the time frame to ask for
   */
  @Override
  public void configure(final HistoryProvider provider, final Timespan timespan) {
    configurePlayer(new HistoryConfiguration(provider, timespan));
  }
  
  /**
   * Configures the player with a time period and a data source
   * 
   * @param historyConfiguration The configuration to set for the player
   */
  public void configurePlayer(final HistoryConfiguration historyConfiguration) {
    HistoryProvider oldHistoryProvider = null;
    if (this.getHistoryConfiguration() != null) {
      try {
        oldHistoryProvider = this.getHistoryConfiguration().getHistoryProvider();
        oldHistoryProvider.removeHistoryProviderListener(this.eventsForwarder);
      }
      catch (NoHistoryProviderAvailableException e) { }
    }
    
    this.historyLoader.setHistoryConfiguration(historyConfiguration);
    
    HistoryProvider historyProvider = null;
    try {
      historyProvider = historyConfiguration.getHistoryProvider();
      historyProvider.addHistoryProviderListener(this.eventsForwarder);
    }
    catch (NoHistoryProviderAvailableException e) {
      LOG.debug("No history provider available, some progressing events will be disabled.", e);
    }
    
    if (oldHistoryProvider != historyProvider && historyProvider != null) {
      // Notifies the listeners about a new history provider
      for (final HistoryPlayerListener listener : historyPlayerListeners.getAll()) {
        listener.onHistoryProviderChanged(historyProvider);
      }
    }
    
    // Sets the clock
    setClockEndTime(this.getStart());
  }
  
  /**
   * Activates the history player
   */
  @Override
  public synchronized void activateHistoryPlayer() {
    if (isHistoryPlayerActive()) {
      LOG.debug("The history mode is already active");
      return;
    }
    
    this.historyModeActive = true;
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Activating history mode");
    }
    
    // Notifies the listeners that the history player is being activated
    fireActivatingHistoryPlayer();
  }
  
  /**
   * Is invoked when the time of history available is changed 
   * 
   * @param newEndTime The new ending time
   */
  private void playbackBufferIntervalUpdated(final Date newEndTime) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Playback available until " + newEndTime.toString());
    }
    
    // Updates the clock
    setClockEndTime(newEndTime);
  }
  
  /**
   * Is invoked when the history is fully loaded
   */
  private void playbackBufferFullyLoaded() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Playback fully loaded");
    }
    
    playbackBufferIntervalUpdated(getEnd());
  }
  
  /**
   * Sets the new time on the clock
   * 
   * @param endTime The new end time to set
   */
  private void setClockEndTime(final Date endTime) {
    playbackControl.setClockTimespan(getStart(), endTime);
  }
  
  /**
   * 
   * @return The ending time of how far in time records have been loaded.
   */
  @Override
  public Timestamp getHistoryLoadedUntil() {
    return this.historyLoader.getHistoryStore().getHistoryIsLoadedUntilTime();
  }

  /**
   * This method is called every time a bunch of history data has been loaded.
   * It updates the new data tags with the proper value and schedules events for
   * all future updates.
   * 
   * @param tagIDs
   *          A collection of the new tag IDs
   */
  private void historyLoaded(final Collection<Long> tagIDs) {
    this.historyScheduler.rescheduleEvents();
  }
  
  /**
   * Call this method to register update listener for a tag
   * 
   * @param tagUpdateListener
   *          The listener to add
   * @param tagId
   *          The tag id the listener will be registered on
   * @param currentRealtimeValue
   *          The current real time value of the tag, is used for filtering. Can
   *          be <code>null</code>, but is not recommended for performance
   * 
   * @see #beginLoading()
   */
  @Override
  public void registerTagUpdateListener(final TagUpdateListener tagUpdateListener, final Long tagId, final ClientDataTagValue currentRealtimeValue) {
    if (isHistoryPlayerActive()) {
      // Registers the listener
      this.publisher.getTagListenersManager().add(tagId, tagUpdateListener);
      
      final TagValueUpdate currentValue;
      if (currentRealtimeValue != null) {
        currentValue = 
          new HistoryTagValueUpdateImpl(
              currentRealtimeValue.getId(), 
              currentRealtimeValue.getDataTagQuality(), 
              currentRealtimeValue.getValue(), 
              currentRealtimeValue.getTimestamp(), 
              currentRealtimeValue.getServerTimestamp(), 
              currentRealtimeValue.getDescription(), 
              currentRealtimeValue.getAlarms().toArray(new AlarmValue[0]), 
              currentRealtimeValue.getMode());
      }
      else {
        currentValue = 
          new HistoryTagValueUpdateImpl(
              tagId, 
              null, 
              null, 
              null, 
              null, 
              null, 
              null, 
              null);
      }
      
      tagsToRegisterLiveValueLock.lock();
      try {
        tagsToRegisterLiveValue.add(currentValue);
      }
      finally {
        tagsToRegisterLiveValueLock.unlock();
      }
    }
  }
  
  /**
   * Registers the listener to the given set of supervision events
   * 
   * @param supervisionListener
   *          the listener to register
   * @param ids
   *          the ids of the entity
   * @param entity
   *          the entity
   * 
   * @see #beginLoading()
   */
  @Override
  public void registerSupervisionListener(final SupervisionEntity entity, final SupervisionListener supervisionListener, final Collection<Long> ids) {
    if (ids == null || ids.size() == 0) {
      return;
    }
    final SupervisionListenersManager supervisionManager = publisher.getSupervisionManager(entity);
    for (final Long id : ids) {
      supervisionManager.add(id, supervisionListener);
    }
    
    // TODO Add something to the history store or similiar?
  }
  
  /**
   * Unregisters the listener from all supervision events it is registered to.
   * 
   * @param entity
   *          The entity to remove the listener from
   * @param listener
   *          The listener to unregister
   */
  @Override
  public void unregisterSupervisionListener(final SupervisionEntity entity, final SupervisionListener listener) {
    //final Collection<Long> removedTags = 
    this.publisher.getSupervisionManager(entity).remove(listener);
    
    // TODO Removes the supervision data from the history store
    // this.historyLoader.getHistoryStore().unregisterSupervision(removedTags);
  }
  
  /**
   * Unregisters the listener from all tags it is registered to.
   * 
   * @param tagUpdateListener
   *          The listener to unregister
   */
  @Override
  public void unregisterTagUpdateListener(final TagUpdateListener tagUpdateListener) {
    final Collection<Long> removedTags = this.publisher.getTagListenersManager().remove(tagUpdateListener);
    
    if (removedTags.size() > 0) {
      // Removes the tags from the history store
      this.historyLoader.getHistoryStore().unregisterTags(removedTags);
    }
  }
  
  /**
   * Unregisters all listeners which are registered on this tag.
   * 
   * @param tagIds
   *          the tag ids to unregister
   */
  @Override
  public void unregisterTags(final Collection<Long> tagIds) {
    for (final Long tagId : tagIds) {
      this.publisher.getTagListenersManager().remove(tagId);
    }
    
    this.historyLoader.getHistoryStore().unregisterTags(tagIds);
  }
  
  /**
   * Call this method after registration of any listeners. Preferably called
   * after registering a bunch of listeners, and not in a loop.
   * 
   * @see #registerTagUpdateListener(TagUpdateListener, Long, TagValueUpdate)
   * @see #registerSupervisionListener(SupervisionEntity, SupervisionListener, Collection)
   */
  @Override
  public void beginLoading() {
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Filtering datatags");
    }
    
    try {
      this.historyLoader.getHistoryStore().setBatching(true);
      
      final Collection<Long> tagsToLoad;
      this.tagsToRegisterLiveValueLock.lock();
      try {
        tagsToLoad = 
          this.historyLoader.getHistoryStore().filterRealTimeValues(
              this.tagsToRegisterLiveValue.toArray(new TagValueUpdate[0]));
        
        this.tagsToRegisterLiveValue.clear();
      }
      finally {
        this.tagsToRegisterLiveValueLock.unlock();
      }
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("Invalidating data tags");
      }
      
      // Invalidates the tags which is not yet loaded
      for (final Long tagId : tagsToLoad) {
        final TagValueUpdate tagValueInvalidation = new HistoryTagValueUpdateImpl(
            tagId, 
            new DataTagQualityImpl(TagQualityStatus.UNINITIALISED, "Loading history records.."), 
            null, 
            null, 
            new Timestamp(System.currentTimeMillis()), 
            "",  
            TagMode.OPERATIONAL);
        publisher.publish(tagValueInvalidation);
      }
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("Registering data tags");
      }
      
      // Register the tag in the history store
      this.historyLoader.getHistoryStore().registerTags(tagsToLoad.toArray(new Long[0]));
    }
    finally {
      this.historyLoader.getHistoryStore().setBatching(false);
    }
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Initiates loading");
    }
    
    historyLoader.beginLoading();
  }
  
  /**
   * 
   * @return <code>true</code> if history player is active, <code>false</code>
   *         otherwise
   */
  public boolean isHistoryPlayerActive() {
    return this.historyModeActive;
  }
  
  /*
   * Methods to invoke methods in all the registered HistoryPlayerListeners
   */
  
  /**
   * Fires the {@link HistoryPlayerListener}.activatingHistoryPlayer()
   */
  protected void fireActivatingHistoryPlayer() {
    for (final HistoryPlayerListener listener : historyPlayerListeners.getAll()) {
      listener.onActivatedHistoryPlayer();
    }
  }
  
  /**
   * Fires the {@link HistoryPlayerListener}.deactivatingHistoryPlayer()
   */
  protected void fireDeactivatingHistoryPlayer() {
    for (final HistoryPlayerListener listener : historyPlayerListeners.getAll()) {
      listener.onDeactivatingHistoryPlayer();
    }
  }
  
  
  /*
   * Methods to add and remove HistoryPlayerListeners
   */
  
  /**
   * 
   * @param listener The listener to add
   */
  public void addHistoryPlayerListener(final HistoryPlayerListener listener) {
    this.historyPlayerListeners.add(listener);
  }
  
  /**
   * 
   * @param listener The listener to remove
   */
  public void removeHistoryPlayerListener(final HistoryPlayerListener listener) {
    this.historyPlayerListeners.remove(listener);
  }
  
  /*
   * Getters
   */
  
  /**
   * 
   * @return The history configuration
   */
  public HistoryConfiguration getHistoryConfiguration() {
    return this.historyLoader.getHistoryConfiguration();
  }
  
  /**
   * @return the start
   */
  @Override
  public Date getStart() {
    try {
      return this.historyLoader.getHistoryConfiguration().getTimespan().getStart();
    }
    catch (NullPointerException e) {
      return null;
    }
  }
  
  /**
   * @return the end
   */
  @Override
  public Date getEnd() {
    try {
      return this.historyLoader.getHistoryConfiguration().getTimespan().getEnd();
    }
    catch (NullPointerException e) {
      return null;
    }
  }
  
  /**
   * 
   * @return A interface which can be used to control the playback
   */
  @Override
  public PlaybackControl getPlaybackControl() {
    return this.playbackControl;
  }
  
  /**
   * 
   * @return A interface which can be used to control the playback
   */
  public PlaybackSynchronizeControl getPlaybackSynchronizeControl() {
    return this.playbackControl;
  }

  /**
   * @return the historyScheduler
   */
  public HistoryScheduler getHistoryScheduler() {
    return historyScheduler;
  }

  /**
   * @return the historyLoader
   */
  public HistoryLoader getHistoryLoader() {
    return historyLoader;
  }

  /**
   * @return the clockSynchronizer
   */
  @Override
  public ClockSynchronizer getClockSynchronizer() {
    return clockSynchronizer;
  }

  /**
   * 
   * @return the publisher
   */
  public HistoryPublisher getPublisher() {
    return publisher;
  }

  @Override
  public HistoryProvider getHistoryProvider() {
    if (getHistoryConfiguration() != null) {
      try {
        return getHistoryConfiguration().getHistoryProvider();
      }
      catch (NoHistoryProviderAvailableException e) {
        
      }
    }
    return null;
  }
  
}
