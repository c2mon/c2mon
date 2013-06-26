/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2013 CERN. This program is free software; you can
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
package cern.c2mon.client.ext.simulator;

import java.util.Map;
import java.util.Set;

import cern.c2mon.client.core.C2monTagManager;
import cern.tim.shared.common.datatag.TagQualityStatus;

/**
 * The tag simulator allows manipulating the value of already registered data
 * tags. The simulator mode works similar to the history viewer and makes a
 * snapshot of the current cache when turned on. During the full simulation phase
 * the incoming updates of the server are ignored. However the live cache will stay
 * up to date and will show the newest values when the simulation mode is turned of.
 *
 * @author Matthias Braeger
 */
public interface C2monTagSimulator {

  /**
   * Starts the simulation mode
   * @return <code>false</code>, if the simulation mode could not be
   *         started. This is the case, if the history mode is turned
   *         on.
   * @see C2monHistoryManager#isHistoryModeEnabled()
   * @see #isSimulationModeEnabled()
   */
  boolean startSimulationMode();
  
  /**
   * Stops the simulation mode and returns into the live mode.
   */
  void stopSimulationMode();
  
  /**
   * @return <code>true</code>, if the simulation mode is activated.
   */
  boolean isSimulationModeEnabled();
  
  /**
   * This method allows changing a particular tag value. Please note, that 
   * the call will be ignored, if the simulation mode is not started or if
   * the tag for the given id has not been registered through the
   * {@link C2monTagManager}.
   * @param tagId The tag id
   * @param value The new value for that tag
   * @throws ClassCastException In case that the given value cannot be
   *         casted to the defined type of the tag
   * @return <code>true</code>, if the tag could be found in the cache and its
   *         value successfully changed.
   */
  boolean changeValue(final Long tagId, final Object value) throws ClassCastException;
  
  /**
   * This method allows changing the values of several tags at the same time.
   * Please note, that the call will be ignored, if the simulation mode is not
   * started.
   * @param tagValues a map containing the simulated value changes with the tag
   *        ids as keys.
   * @return <code>true</code>, if all listed tags could successfully be updated.
   *         The call will return <code>false</code>, in case one of the defined
   *         tag updates can't be performed. Possible reasons can be a class cast
   *         exception during the update or because a tag has not been registered
   *         before through the {@link C2monTagManager}.
   */
  boolean changeValues(Map<Long, Object> tagValues);
  
  /**
   * Allows invalidating a tag. To re-validate it later again, just make again a 
   * {@link #changeValue(Long, Object)} call for the same tag.
   * @param tagId The tag id
   * @param status The invalidation status
   * @return <code>true</code>, if the tag could be found in the cache and its
   *         quality status successfully changed.
   */
  boolean invalidateTag(final Long tagId, final TagQualityStatus status);
  
  /**
   * Allows invalidating multiple tags with the same quality status. To re-validate
   * them later again, just make again a {@link #changeValues(Map)} call for the
   * same list of tags.
   * @param tagIds list of tag ids
   * @param status the invalidation status
   * @return <code>true</code>, if all tags could be found in the cache
   *         and have successfully been updated. 
   */
  boolean invalidateTags(final Set<Long> tagIds, final TagQualityStatus status);
}
