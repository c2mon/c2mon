/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.cache.tag;

import java.sql.Timestamp;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

/**
 * Interface exposing common methods for modifying Tag objects 
 * (DataTags, ControlTags and RuleTags). These are implemented
 * in the {@link AbstractTagObjectFacade} class.
 * 
 * <p>Notice these methods do not synchronise access to the Tags,
 * so should be called within a lock (or preferably, use the associated
 * CommonTagFacade implementation.
 * 
 * @param <T> the Tag this Facade bean acts on
 * @author Mark Brightwell
 *
 */
public interface CommonTagObjectFacade<T extends Tag> {

  /**
   * Maximum length of the value description. If the user tries to send a
   * longer value description, it will be truncated.
   */
  int MAX_DESC_LENGTH = 1000;
  
  /**
   * Validates the Tag object by setting the quality to valid.
   * 
   * @param tag the Tag object to validate
   */
  void validate(T tag);

  /**
   * Adds the quality flag to the current quality code of the DataTag with associated
   * description.
   * 
   * @param dataTag the cache object
   * @param statusToAdd the flag to add to the quality code
   * @param description the description associated to this tag
   */
  void addQualityFlag(T dataTag, TagQualityStatus statusToAdd, String description);

  /**
   * Updates the quality of the Tag object. The provided status is added as quality flag with
   * the associated description. The description can be left as null (any
   * older description for this flag will be overwritten).
   * 
   * <p>The object is not locked in this method.
   * 
   * @param tag the tag to update
   * @param qualityStatusToAdd status that is added
   * @param description associated with this status; can be null if no description is required
   */
  void updateQuality(T tag, TagQualityStatus qualityStatusToAdd, String description);
  
  /**
   * Sets the cacheTimestamp.
   * @param tag the tag to update
   * @param timestamp the new timestamp value
   */
  void setCacheTimestamp(T tag, Timestamp timestamp);

  /**
   * Updates the tag with the given value (no filtering of any kind is performed).
   * The value description is also set if provided. If the description is passed
   * as null, will attempt to set the description from an attached value dictionary.
   * 
   * 
   * @param tag the Tag to update
   * @param value the new value
   * @param valueDesc the new value description
   */
  void updateValue(T tag, Object value, String valueDesc);
  
  
  
}
