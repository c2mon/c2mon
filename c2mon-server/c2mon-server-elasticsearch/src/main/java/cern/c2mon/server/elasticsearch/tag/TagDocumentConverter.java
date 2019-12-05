/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.elasticsearch.tag;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.type.TypeConverter;

/**
 * Converts {@link Tag} instances to {@link TagDocument} instances.
 *
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 */
@Slf4j
@Component
public class TagDocumentConverter extends BaseTagDocumentConverter<TagDocument> {

  @Autowired
  public TagDocumentConverter(final ProcessCache processCache, final EquipmentCache equipmentCache, final SubEquipmentCache subEquipmentCache) {
    super(processCache, equipmentCache, subEquipmentCache, TagDocument::new);
  }

  @Override
  public Optional<TagDocument> convert(final Tag tag) {
    try {
      return super.convert(tag).map(tagDocument -> fillAdditionalData(tagDocument, tag));
    } catch (Exception e) {
      log.error("Error occurred during conversion of Tag #{} ({}) to Elasticsearch document. Unable to store update to Elasticsearch!", tag.getId(), tag.getName(), e);
    }
    return Optional.empty();
  }

  private TagDocument fillAdditionalData(TagDocument tagDocument, Tag tag) {
    tagDocument.put("timestamp", tag.getTimestamp().getTime());
    tagDocument.put("quality", getQuality(tag));
    tagDocument.put("valueDescription", tag.getValueDescription());
    Class<?> clazz = TypeConverter.getType(tag.getDataType());
    if (clazz == null) {
      tagDocument.put("valueObject", tag.getValue());

    } else if (Number.class.isAssignableFrom(clazz)) {
      tagDocument.put("value", tag.getValue());

      if (Long.class.isAssignableFrom(clazz)) {
        tagDocument.put("valueLong", tag.getValue());
      }
    } else if (Boolean.class.isAssignableFrom(clazz)) {
      tagDocument.put("valueBoolean", tag.getValue());

      if (tag.getValue() != null) {
        tagDocument.put("value", tag.getValue() != null ? 1 : 0);
      }
    } else if (String.class.isAssignableFrom(clazz)) {
      tagDocument.put("valueString", tag.getValue());

    } else {
      tagDocument.put("valueObject", tag.getValue());
    }
    return tagDocument;
  }

  @Override
  protected Map<String, Object> getC2monMetadata(Tag tag) {
    Map<String, Object> map = super.getC2monMetadata(tag);
    map.put("serverTimestamp", tag.getCacheTimestamp().getTime());

    if (tag instanceof DataTag) {
      DataTag dataTag = (DataTag) tag;

      if (dataTag.getDaqTimestamp() != null) {
        map.put("daqTimestamp", dataTag.getDaqTimestamp().getTime());
      }

      if (dataTag.getSourceTimestamp() != null) {
        map.put("sourceTimestamp", dataTag.getSourceTimestamp().getTime());
      }
    }

    return map;
  }

  private Map<String, Object> getQuality(Tag tag) {
    Map<String, Object> map = new HashMap<>();
    DataTagQuality quality = tag.getDataTagQuality();

    map.put("valid", quality.isValid());
    map.put("status", calculateStatus(tag));
    map.put("statusInfo", collectStatusInfo(quality));

    return map;
  }

  /**
   * Calculates the accumulated status of a {@link Tag}, based on the
   * individual quality statuses that it contains.
   * <p>
   * TODO: rewrite this to be somewhat readable. It looks like the guy who
   * wrote it just found out about java 8 and got a bit over-excited.
   *
   * @param tag the {@link Tag} instance that is used to calculate the status
   * @return the result of the accumulated statuses.
   * If no invalid status was found (good quality) the result will be {@code 0}.
   */
  private int calculateStatus(final Tag tag) {
    return Optional.ofNullable(tag.getDataTagQuality())
        .map(DataTagQuality::getInvalidQualityStates)
        .map(Map::keySet)
        .map(tagQualityStatuses -> tagQualityStatuses.stream()
            .mapToInt(TagQualityStatus::getCode)
            .map(statusCode -> (int) Math.pow(2, statusCode))
            .sum())
        .orElse(0);
  }

  /**
   * Collects the individual statuses with their description, if any, based on
   * a {@link DataTagQuality} instance.
   *
   * @param dataTagQuality the tag quality instance, that contains the
   *                       individual quality information
   * @return a {@link Collection} of invalid statuses or a {@link Collection}
   * with a single value {@code "OK"}, if no invalid qualities were found
   */
  private Collection<String> collectStatusInfo(final DataTagQuality dataTagQuality) {
    Map<TagQualityStatus, String> invalidQualityStates = dataTagQuality.getInvalidQualityStates();

    if (invalidQualityStates == null) {
      return Collections.singleton("OK");
    }

    Collection<String> invalidQualityInfo = invalidQualityStates.entrySet().stream()
        .map(invalidQualityState -> String.format("%s : %s",
            invalidQualityState.getKey().name(), invalidQualityState.getValue()))
        .collect(Collectors.toSet());

    if (invalidQualityInfo.isEmpty()) {
      return Collections.singleton("OK");
    }

    return invalidQualityInfo;
  }
}
