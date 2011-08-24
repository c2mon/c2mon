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
package cern.c2mon.client.common.history;

import java.sql.Timestamp;
import java.util.Collection;

import cern.c2mon.client.common.history.event.HistoryLoadingManagerListener;
import cern.c2mon.client.common.history.exception.LoadingParameterException;
import cern.c2mon.client.common.history.id.SupervisionEventId;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.tim.shared.common.supervision.SupervisionConstants.SupervisionEntity;

/**
 * 
 * This interface describes the functions for a history loading manager.
 * 
 * @author vdeila
 * 
 */
public interface HistoryLoadingManager {

  /**
   * Cannot be set while loading ({@link #isLoading()} == true)
   * 
   * @param configuration
   *          the loading parameters
   */
  void setConfiguration(HistoryLoadingConfiguration configuration);

  /**
   * Begins the loading process. This function will return immediately, a thread
   * will be started to do the loading.
   * 
   * @throws LoadingParameterException
   *           if the {@link #setConfiguration(HistoryLoadingConfiguration)} is
   *           not set or any of the parameters is invalid.
   */
  void beginLoading() throws LoadingParameterException;
  
  /**
   * Begins the loading process. This function will return immediately if async
   * is set to <code>true</code>.
   * 
   * @param async
   *          <code>false</code> to not run the loading process in a seperate
   *          thread. the default is <code>true</code>
   * 
   * @throws LoadingParameterException
   *           if the {@link #setConfiguration(HistoryLoadingConfiguration)} is
   *           not set or any of the parameters is invalid.
   */
  void beginLoading(boolean async) throws LoadingParameterException;

  /**
   * 
   * @return <code>true</code> if it is currently loading.
   */
  boolean isLoading();

  /**
   * Stops the loading process
   * 
   * @param wait
   *          <code>true</code> to wait for the loading to stop before
   *          continuing.
   */
  void stopLoading(boolean wait);

  /**
   * Adds the tag AND all the supervision events connected to it in the loading
   * queue.<br/><br/>
   * 
   * The <code>tag</code> must be a COPY!
   * 
   * @param tag
   *          the tag to add, a copy, as this instance will be used for combining it with 
   *          the supervision events.
   */
  void addClientDataTagForLoading(ClientDataTag tag);
  
  /**
   * Adds the tags AND all the supervision events connected to them<br/>
   * <br/>
   * 
   * The <code>tags</code> must be COPIES!
   * 
   * @param tags
   *          the tags to add, copies, as these instances will be used for
   *          combining it with the supervision events.
   */
  void addClientDataTagsForLoading(Collection<ClientDataTag> tags);

  /**
   * 
   * @param tagId
   *          the tag id
   * @return all history of the tag, mixed with the supervision events that it
   *         is connected to. The list is sorted.
   */
  Collection<HistoryUpdate> getAllHistory(Long tagId);

  /**
   * 
   * @param tagId
   *          the tag id
   * @return all history of the tag, where all supervision events is converted
   *         into {@link HistoryTagValueUpdate} containing the value of the
   *         previous value (sorted by daq time)
   */
  Collection<HistoryTagValueUpdate> getAllHistoryConverted(Long tagId);

  /**
   * 
   * @param tagId
   *          the tag id
   * @return the history of the given tag
   */
  Collection<HistoryTagValueUpdate> getHistory(Long tagId);

  /**
   * 
   * @param supervisionEventId
   *          the supervision identifier
   * @return the supervision event history of the given identifier
   */
  Collection<HistorySupervisionEvent> getHistory(SupervisionEventId supervisionEventId);

  /**
   * Gets one specific set of supervision data
   * 
   * @param entity
   *          the supervision entity
   * @param entityId
   *          the entity id
   * @return the supervision event history of the given id and entity
   */
  Collection<HistorySupervisionEvent> getHistory(SupervisionEntity entity, Long entityId);

  /**
   * 
   * @return the earliest time which have been loaded (exluding initial data)
   */
  Timestamp getEarliestTimeLoaded();

  /**
   * 
   * @return the latest time which have been loaded
   */
  Timestamp getLatestTimeLoaded();
  
  /**
   * 
   * @param listener
   *          the listener to add
   */
  void addHistoryLoadingManagerListener(HistoryLoadingManagerListener listener);

  /**
   * 
   * @param listener
   *          the listener to remove
   */
  void removeHistoryLoadingManagerListener(HistoryLoadingManagerListener listener);

}
