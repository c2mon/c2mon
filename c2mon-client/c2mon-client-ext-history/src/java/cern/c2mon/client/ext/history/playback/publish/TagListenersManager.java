/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.client.ext.history.playback.publish;

import java.util.Collection;

import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.client.ext.history.util.KeyForValuesMap;

/**
 * This class keeps lists of which listeners is listening on which tag.
 * 
 * @author vdeila
 * 
 */
public class TagListenersManager extends KeyForValuesMap<Long, TagUpdateListener> {

  /**
   * Constructor
   */
  public TagListenersManager() {
    super();
  }

  /**
   * 
   * @param tagId
   *          The tag id the listener wants to listen to
   * @param listener
   *          The listener to add
   * @return <code>true</code> if this is the first listener registered on the
   *         tag
   */
  @Override
  public boolean add(final Long tagId, final TagUpdateListener listener) {
    return super.add(tagId, listener);
  }

  /**
   * Removes all listeners
   */
  @Override
  public void clear() {
    super.clear();
  }

  /**
   * 
   * @param tagId
   *          the tag id to get the listeners for
   * @return the listeners registered on the <code>tagId</code>
   */
  @Override
  public Collection<TagUpdateListener> getValues(final Long tagId) {
    return super.getValues(tagId);
  }

  /**
   * 
   * @param tagId
   *          tag id
   * @return <code>true</code> if the tag id have listeners
   */
  @Override
  public boolean haveKey(final Long tagId) {
    return super.haveKey(tagId);
  }

  /**
   * 
   * @param listener
   *          The listener to remove
   * @return a list of tags which doesn't have any listeners after the removal
   *         of this listener
   */
  public Collection<Long> remove(final TagUpdateListener listener) {
    return super.removeValue(listener);
  }

  /**
   * All listeners will be removed from the given tag.
   * 
   * @param tagId
   *          the tag to remove.
   */
  public void remove(final Long tagId) {
    super.removeKey(tagId);
  }
}
