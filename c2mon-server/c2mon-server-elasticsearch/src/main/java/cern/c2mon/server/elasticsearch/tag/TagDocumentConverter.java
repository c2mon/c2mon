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
package cern.c2mon.server.elasticsearch.tag;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.metadata.Metadata;
import cern.c2mon.shared.common.type.TypeConverter;

/**
 * Converts {@link Tag} instances to {@link TagDocument} instances.
 *
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 */
@Slf4j
@Component
public class TagDocumentConverter implements Converter<Tag, TagDocument> {

  private final ProcessCache processCache;
  private final EquipmentCache equipmentCache;
  private final SubEquipmentCache subEquipmentCache;

  @Autowired
  public TagDocumentConverter(final ProcessCache processCache,
                              final EquipmentCache equipmentCache,
                              final SubEquipmentCache subEquipmentCache) {
    this.processCache = processCache;
    this.equipmentCache = equipmentCache;
    this.subEquipmentCache = subEquipmentCache;
  }

  @Override
  public TagDocument convert(final Tag tag) {
    Map<String, Object> map = new HashMap<>();

    map.put("id", tag.getId());
    map.put("name", tag.getName());
    map.put("timestamp", tag.getTimestamp().getTime());
    map.put("unit", tag.getUnit());
    map.put("valueDescription", tag.getValueDescription());
    map.put("c2mon", getC2monMetadata(tag));
    map.put("metadata", getMetadata(tag));
    map.put("quality", getQuality(tag));

    Class<?> clazz = TypeConverter.getType(tag.getDataType());

    if (clazz == null) {
      map.put("valueObject", tag.getValue());

    } else if (Number.class.isAssignableFrom(clazz)) {
      map.put("value", tag.getValue());

      if (Long.class.isAssignableFrom(clazz)) {
        map.put("valueLong", tag.getValue());
      }
    } else if (Boolean.class.isAssignableFrom(clazz)) {
      map.put("valueBoolean", tag.getValue());

      if (tag.getValue() != null) {
        map.put("value", tag.getValue() != null ? 1 : 0);
      }
    } else if (String.class.isAssignableFrom(clazz)) {
      map.put("valueString", tag.getValue());

    } else {
      map.put("valueObject", tag.getValue());
    }

    TagDocument tagDocument = new TagDocument();
    tagDocument.putAll(map);
    return tagDocument;
  }

  private Map<String, Object> getC2monMetadata(Tag tag) {
    Map<String, Object> map = new HashMap<>();

    map.put("dataType", tag.getDataType());

    if (!tag.getProcessIds().isEmpty()) {
      Process process = processCache.get(tag.getProcessIds().iterator().next());
      map.put("process", process.getName());
    }

    if (!tag.getEquipmentIds().isEmpty()) {
      Equipment equipment = equipmentCache.get(tag.getEquipmentIds().iterator().next());
      map.put("equipment", equipment.getName());
    }

    if (!tag.getSubEquipmentIds().isEmpty()) {
      SubEquipment subEquipment = subEquipmentCache.get(tag.getSubEquipmentIds().iterator().next());
      map.put("subEquipment", subEquipment.getName());
    }

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

  private Map<String, Object> getMetadata(Tag tag) {
    Metadata metadata = tag.getMetadata();

    if (metadata != null) {
      return metadata.getMetadata().entrySet().stream().collect(Collectors.toMap(
          Map.Entry::getKey,
          e -> e.getValue() == null ? null : e.getValue()
      ));
    }

    return Collections.emptyMap();
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
   *
   * TODO: rewrite this to be somewhat readable. It looks like the guy who
   * wrote it just found out about java 8 and got a bit over-excited.
   *
   * @param tag the {@link Tag} instance that is used to calculate the status
   * @return the result of the accumulated statuses.
   *         If no invalid status was found (good quality) the result will be {@code 0}.
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
