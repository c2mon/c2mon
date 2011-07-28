/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
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
 ******************************************************************************/
package cern.c2mon.client.core.manager;

import java.util.Collection;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.history.HistoryPlayer;
import cern.c2mon.client.common.history.HistoryPlayerEvents;
import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistoryProviderType;
import cern.c2mon.client.common.history.Timespan;
import cern.c2mon.client.common.history.exception.HistoryPlayerNotActiveException;
import cern.c2mon.client.common.history.exception.NoHistoryProviderException;
import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.core.C2monHistoryManager;
import cern.c2mon.client.core.cache.BasicCacheHandler;
import cern.c2mon.client.core.listener.TagSubscriptionListener;
import cern.c2mon.client.history.HistoryProviderFactory;
import cern.c2mon.client.history.dbaccess.exceptions.HistoryException;
import cern.c2mon.client.history.playback.HistoryPlayerCoreAccess;
import cern.c2mon.client.history.playback.HistoryPlayerImpl;
import cern.c2mon.client.jms.SupervisionListener;
import cern.c2mon.shared.client.supervision.SupervisionConstants.SupervisionEntity;

@Service
public class HistoryManager implements C2monHistoryManager, TagSubscriptionListener {

  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(HistoryManager.class);
  
  /** Reference to the <code>TagManager</code> singleton */
  private final CoreTagManager tagManager;

  /** Reference to the <code>ClientDataTagCache</code> */
  private final BasicCacheHandler cache;
  
  /** the history player */
  private HistoryPlayerCoreAccess historyPlayer;

  @Autowired
  protected HistoryManager(final CoreTagManager pTagManager, final BasicCacheHandler pCache) {
    this.tagManager = pTagManager;
    this.cache = pCache;
    this.historyPlayer = null;
  }

  /**
   * Inner method to initialize the STL database connection.
   */
  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    tagManager.addTagSubscriptionListener(this);
  }

  @Override
  public void startHistoryPlayerMode(final HistoryProvider provider, final Timespan timespan) {
    cache.setHistoryMode(true);

    synchronized (cache.getHistoryModeSyncLock()) {
      if (cache.isHistoryModeEnabled()) {
        if (this.historyPlayer == null) {
          this.historyPlayer = new HistoryPlayerImpl();
        }
        
        // Configures the history player with the given provider and time span
        this.historyPlayer.configure(provider, timespan);
        
        // Activating the history player
        this.historyPlayer.activateHistoryPlayer();
        
        // Subscribes all the current subscribed tags
        subscribeTagsToHistory(cache.getAllSubscribedDataTags());
      }
    }
  }

  @Override
  public void stopHistoryPlayerMode() {
    cache.setHistoryMode(false);
    
    synchronized (cache.getHistoryModeSyncLock()) {
      if (this.historyPlayer != null) {
        this.historyPlayer.deactivateHistoryPlayer();
      }
    }
  }

  @Override
  public void onNewTagSubscriptions(final Set<Long> tagIds) {
    synchronized (cache.getHistoryModeSyncLock()) {
      if (cache.isHistoryModeEnabled()
          && this.historyPlayer != null 
          && this.historyPlayer.isHistoryPlayerActive()) {
        subscribeTagsToHistory(tagIds);
      }
    }
  }

  @Override
  public void onUnsubscribe(final Set<Long> tagIds) {
    unsubscribeTagsFromHistory(tagIds);
  }

  /**
   * Subscribes the tags to the history player
   * 
   * @param tagIds
   *          The tags to subscribes
   */
  private void subscribeTagsToHistory(final Set<Long> tagIds) {
    subscribeTagsToHistory(cache.get(tagIds).values());
  }

  /**
   * Subscribes the tags to the history player
   * 
   * @param clientDataTags
   *          The tags to subscribes
   */
  private void subscribeTagsToHistory(final Collection<ClientDataTag> clientDataTags) {
    synchronized (cache.getHistoryModeSyncLock()) {
      if (cache.isHistoryModeEnabled()
          && this.historyPlayer != null 
          && this.historyPlayer.isHistoryPlayerActive()) {
        
        // Registers all the tag update listeners and supervision listeners  
        for (final ClientDataTag cdt : clientDataTags) {
          
          
          // Registers to tag updates
          try {
            this.historyPlayer.registerTagUpdateListener(
                (TagUpdateListener) cdt,
                cdt.getId(),
                cdt.clone());
          }
          catch (CloneNotSupportedException e) {
            LOG.error("subscribeTagsToHistory() - Unable to clone ClientDataTag with id " + cdt.getId(), e);
            throw new UnsupportedOperationException("Unable to clone ClientDataTag with id " + cdt.getId(), e);
          }
          
          // Register to supervision events
          this.historyPlayer.registerSupervisionListener(
              SupervisionEntity.PROCESS,
              (SupervisionListener) cdt,
              cdt.getProcessIds());
          
          this.historyPlayer.registerSupervisionListener(
              SupervisionEntity.EQUIPMENT,
              (SupervisionListener) cdt,
              cdt.getEquipmentIds());
          
//          For the day the SupervisionEntity.SUBEQUIPMENT also comes
//          this.historyPlayer.registerSupervisionListener(
//              SupervisionEntity.SUBEQUIPMENT,
//              (SupervisionListener) clientDataTag,
//              clientDataTag.getSubEquipmentIds());
        }
        
        // Begins the loading process
        this.historyPlayer.beginLoading();
      }
    }
  }

  /**
   * Unsubscribes the tags to the history player
   * 
   * @param tagIds
   *          The tags to unsubscribe
   */
  private void unsubscribeTagsFromHistory(final Set<Long> tagIds) {
    synchronized (cache.getHistoryModeSyncLock()) {
      if (cache.isHistoryModeEnabled()
          && this.historyPlayer != null 
          && this.historyPlayer.isHistoryPlayerActive()) {
        
        this.historyPlayer.unregisterTags(tagIds);

        //this.historyPlayer.unregisterSupervisionListener(entity, listener)
        // TODO Unregister the datatags
        // TODO Unregister the equipment events, and the process events
      }
    }
    
  }

  @Override
  public HistoryPlayer getHistoryPlayer() throws HistoryPlayerNotActiveException {
    if (isHistoryModeEnabled()) {
      return historyPlayer;
    }
    else {
      throw new HistoryPlayerNotActiveException("The history player is not active, and can therefore not be retrieved");
    }
  }
  
  @Override
  public HistoryPlayerEvents getHistoryPlayerEvents() {
    synchronized (cache.getHistoryModeSyncLock()) {
      if (this.historyPlayer != null) {
        this.historyPlayer = new HistoryPlayerImpl(); 
      }
      return this.historyPlayer;
    }
  }

  @Override
  public HistoryProvider getHistoryProvider(final HistoryProviderType type) throws NoHistoryProviderException {
    try {
      return HistoryProviderFactory.getInstance().createHistoryProvider(type);
    }
    catch (final HistoryException e) {
      throw new NoHistoryProviderException("Failed to get a history provider", e);
    }
  }

  @Override
  public boolean isHistoryModeEnabled() {
    return cache.isHistoryModeEnabled();
  }

}
