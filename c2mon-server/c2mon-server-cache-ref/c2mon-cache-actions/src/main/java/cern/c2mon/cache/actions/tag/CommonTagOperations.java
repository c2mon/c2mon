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
package cern.c2mon.cache.actions.tag;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

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
public interface CommonTagOperations<T extends Tag> {

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

  static boolean isReadyForEvaluation(Tag tag) {
    return (
      (tag != null)
        && (tag.getValue() != null)
        && (tag.isValid())
        && (tag.getDataTagQuality().isInitialised())
        && (tag.getTimestamp() != null)
    );
  }

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

  /**
   * As opposed to the <code>invalidate()</code> methods, this method allows fine grained control of the quality of the
   * datatag: in a single call, multiple quality flags can be added and/or removed and and new quality description
   * can optionally be given.
   *
   * <p>To be used for internal server invalidation/validations as only sets the cache timestamp!
   *
   * <p><b>IMPORTANT:</b> this method should be used in preference to multiple calls the the invalidate() method since it results
   * in a SINGLE notification to the cache listeners (each call to invalidate() results in a new object been passed to all
   * module listeners).
   *
   * <p>This method is (write-)synchronized on the DataTag object.
   *
   * @param tagId unique id of the Tag
   * @param flagsToAdd added flags
   * @param flagsToRemove removed flags
   * @param qualityDescriptions for flags that are set, will attempt to retrieve descriptions from this map
   * @param timestamp sets the cache timestamp
   *
   */
  void setQuality(Long tagId, Collection<TagQualityStatus> flagsToAdd, Collection<TagQualityStatus> flagsToRemove,
                  Map<TagQualityStatus, String> qualityDescriptions, Timestamp timestamp);

}
