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
package cern.c2mon.server.cache.tag;

import java.sql.Timestamp;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

/**
 * Implementation of the common functionality for modifying Tag objects.
 *
 * These facade objects should synchronize access to the Tag objects (which
 * will then be used by Terracotta to provide cluster-wide synchronization).
 *
 * @param <T> the Tag class this Facade acts on
 */
@Slf4j
public abstract class AbstractTagObjectFacade<T extends Tag> implements CommonTagObjectFacade<T> {

  @Override
  public void validate(T tag) {
    tag.getDataTagQuality().validate();
  }

  /**
   * Call within synch.
   * @param tag
   * @param value
   * @param valueDesc
   */
  @Override
  public void updateValue(final T tag, final Object value, final String valueDesc) {
    //cast the passed object to the module cache object implementation
    //to access setter methods (not provided in the common interface)
    AbstractTagCacheObject abstractTag = (AbstractTagCacheObject) tag;

    //update the value... need to adjust this for no obj creation
    abstractTag.setValue(value);

    if (valueDesc != null) {
      if (valueDesc.length() > MAX_DESC_LENGTH) {
        log.warn("Detected oversized value description for tag {} - is being truncated (max size is set at {})",
            tag.getId(), MAX_DESC_LENGTH);
        abstractTag.setValueDescription(valueDesc.substring(0, MAX_DESC_LENGTH));
      } else {
        abstractTag.setValueDescription(valueDesc);
      }
    }
  }

  @Override
  public void updateQuality(final T tag, final TagQualityStatus qualityStatusToAdd, final String description) {
    tag.getDataTagQuality().addInvalidStatus(qualityStatusToAdd, description);
  }

  @Override
  public void addQualityFlag(final T tag, final TagQualityStatus statusToAdd, final String description) {
    tag.getDataTagQuality().addInvalidStatus(statusToAdd, description);
  }

  @Override
  public void setCacheTimestamp(final T tag, final Timestamp timestamp) {
    AbstractTagCacheObject abstractTagCacheObject = (AbstractTagCacheObject) tag;
    abstractTagCacheObject.setCacheTimestamp(timestamp);
  }

}
