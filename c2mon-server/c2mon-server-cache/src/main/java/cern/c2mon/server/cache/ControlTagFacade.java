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
package cern.c2mon.server.cache;

import java.sql.Timestamp;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.thread.Event;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;

/**
 * Facade bean containing the logic for interacting with
 * control tags. Notice the DataTagFacade is sometime used
 * for this purpose since ControlTagCacheObject extends
 * DataTagCacheObject (could be changed in the future
 * if the Control tag evolves).
 * 
 * @author Mark Brightwell
 *
 */
public interface ControlTagFacade extends CommonTagFacade<ControlTag> {

  /**
   * Generates a {@link SourceDataTag} object from the given control tag
   * @param controlTag The control tag which shall be converted
   * @return The resulting source data tag
   */
  SourceDataTag generateSourceDataTag(ControlTag controlTag);

  /**
   * Is this ControlTag in the list of tags on the DAQ level?
   * Currently this is determined by whether the ControlTag
   * is an aliveTag and has a DataTagAddress.
   * 
   * @param controlTag
   * @return
   */
  boolean isInProcessList(ControlTag controlTag);


  /**
   * Validates the state tag used to publish the status to clients and updates it with the provided value.
   * Should be used to update values within the server (only cache timestamp is set).
   * 
   * <p>The cache timestamp is set to the passed time and the DAQ and source timestamps are
   * reset to null.
   * 
   * <p>If the update causes no changes, the cache object is not updated (see filterout method in AbstracTagFacade).
   * 
   * <p>Notifies registered listeners if an update takes place.
   * 
   * @param stateTagId the id of the state tag used to publish the status to clients.
   * @param value the new state tag value
   * @param valueDescription the description of the new value (if any)
   * @param timestamp the time of the update
   */
  void updateAndValidate(Long stateTagId, Object value, String message, Timestamp refreshTime);
  
  /**
   * Updates the ControlTag in the cache from the passed SourceDataTagValue. The method notifies
   * any cache listeners if an update is made. 
   * 
   * <p>The cache timestamp is set to the current time. The DAQ and source timestamps are
   * set to the values received in the SourceDataTagValue.
   * 
   * @param controlTagId id of ControlTag
   * @param sourceDataTagValue the value received from the data acquisition layer
   * @return true if the tag was indeed updated (that is, the cache was modified, i.e. the update was not
   * filtered out for some reason), together with the cache timestamp of this update
   * @throws CacheElementNotFoundException if the Tag cannot be found in the cache
   */
  Event<Boolean> updateFromSource(final Long controlTagId, final SourceDataTagValue sourceDataTagValue);
}
