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

import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

/**
 * Interface exposing common methods for modifying Tag objects
 * (DataTags, ControlTags and RuleTags).
 *
 * @author Mark Brightwell, Alexandros Papageorgiou
 */
@Slf4j
public class TagController {

  /**
   * Maximum length of the value description. If the user tries to send a
   * longer value description, it will be truncated.
   */
  public static final int MAX_DESC_LENGTH = 1000;

  private TagController() {
  }

  /**
   * Validates the Tag object by setting the quality to valid.
   *
   * @param tag the Tag object to validate
   */
  public static void validate(Tag tag) {
    tag.getDataTagQuality().validate();
  }

  /**
   * Adds the quality flag to the current quality code of the Tag with associated
   * description.
   *
   * @param tag     the cache object
   * @param statusToAdd the flag to add to the quality code
   * @param description the description associated to this tag
   */
  public static void addQualityFlag(Tag tag, TagQualityStatus statusToAdd, String description){
    tag.getDataTagQuality().addInvalidStatus(statusToAdd, description);
  }

  /**
   * Updates the quality of the Tag object. The provided status is added as quality flag with
   * the associated description. The description can be left as null (any
   * older description for this flag will be overwritten).
   *
   * @param tag                the tag to update
   * @param qualityStatusToAdd status that is added
   * @param description        associated with this status; can be null if no description is required
   */
  public static void updateQuality(Tag tag, TagQualityStatus qualityStatusToAdd, String description) {
    tag.getDataTagQuality().addInvalidStatus(qualityStatusToAdd, description);
  }

  /**
   * Updates the tag with the given value (no filtering of any kind is performed).
   * The value description is also set if provided. If the description is passed
   * as null, will attempt to set the description from an attached value dictionary.
   *
   * @param tag       the Tag to update
   * @param value     the new value
   * @param valueDesc the new value description
   */
  public static void setValue(Tag tag, Object value, String valueDesc) {
    AbstractTagCacheObject abstractTag = (AbstractTagCacheObject) tag;

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
   * @param tag                 the Tag to update
   * @param flagsToAdd          added flags
   * @param flagsToRemove       removed flags
   * @param qualityDescriptions for flags that are set, will attempt to retrieve descriptions from this map
   * @param timestamp           sets the cache timestamp
   */
  public static void setQuality(Tag tag, Collection<TagQualityStatus> flagsToAdd, Collection<TagQualityStatus> flagsToRemove,
                  Map<TagQualityStatus, String> qualityDescriptions, Timestamp timestamp) {
    if (flagsToRemove == null && flagsToAdd == null) {
      log.warn("Attempting to set quality in TagFacade with no Quality flags to remove or set!");
    }

    if (flagsToRemove != null) {
      for (TagQualityStatus status : flagsToRemove) {
        tag.getDataTagQuality().removeInvalidStatus(status);
      }
    }
    if (flagsToAdd != null) {
      for (TagQualityStatus status : flagsToAdd) {
        tag.getDataTagQuality().addInvalidStatus(status, qualityDescriptions.get(status));
      }
    }
    tag.setCacheTimestamp(timestamp);
  }

  /**
   * Checks if the new Tag value should be filtered out or updated.
   * Is filtered out if value, value description and quality are
   * the same.
   *
   * @param valueDescription the new description
   * @param value the new value
   * @param tag the tag that is updated
   * @param statusToAdd the tag quality status to add; leave null if the tag is to be validated
   * @param statusDescription the new status description; leave null if the tag is to be validated
   * @return true if it should be filtered out
   * @throws NullPointerException if called with null tag parameter
   * @throws IllegalArgumentException if status description is not null but statusToAdd is (does not make any sense!) or the same for the value
   */
  public static boolean filterout(Tag tag, Object value, String valueDescription,
                           TagQualityStatus statusToAdd, String statusDescription) {
    if (statusToAdd == null && statusDescription != null) {
      throw new IllegalArgumentException("Filterout method called with non-null status description but null status");
    }
    if (value == null && valueDescription != null) {
      throw new IllegalArgumentException("Filterout method called with non-null value description but null value");
    }
    boolean sameValue;
    if (tag.getValue() != null){
      sameValue = tag.getValue().equals(value);
    } else {
      sameValue = (value == null);
    }
    if (!sameValue) {
      return false;
    }

    boolean sameDescription;
    if (tag.getValueDescription() != null){
      sameDescription = tag.getValueDescription().equalsIgnoreCase(valueDescription);
    } else {
      sameDescription = (valueDescription == null);
    }
    if (!sameDescription) {
      return false;
    }

    boolean sameQuality;
    if (statusToAdd == null){
      sameQuality = tag.getDataTagQuality().isValid();
    } else {
      sameQuality = (tag.getDataTagQuality() != null
        && tag.getDataTagQuality().isInvalidStatusSetWithSameDescription(statusToAdd, statusDescription));
    }
    return sameQuality;
  }

  /**
   * As for general filterout method, but for invalidation only.
   * @param tag the current tag
   * @param statusToAdd the status to add
   * @param statusDescription the status description to use
   * @return true if should be filtered
   */
  public static boolean filteroutInvalidation(Tag tag, TagQualityStatus statusToAdd, String statusDescription) {
    return filterout(tag, tag.getValue(), tag.getValueDescription(), statusToAdd, statusDescription);
  }

  /**
   * As for general filterout method, but for valid updates only.
   * @param tag the current tag
   * @param value the new value
   * @param valueDescription the new value description
   * @return true if should be filtered
   */
  public static boolean filteroutValid(Tag tag, Object value, String valueDescription) {
    return filterout(tag, value, valueDescription, null, null);
  }

}
