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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.tag.Tag;

/**
 * @author Justin Lewis Salmon
 */
@Slf4j
public class BaseTagDocumentConverter<T extends Map<String, Object>> implements Converter<Tag, Optional<T>> {

  private final ProcessCache processCache;
  private final EquipmentCache equipmentCache;
  private final SubEquipmentCache subEquipmentCache;
  private final Supplier<T> containerSupplier;

  public BaseTagDocumentConverter(final ProcessCache processCache, final EquipmentCache equipmentCache, final SubEquipmentCache subEquipmentCache, final Supplier<T> containerSupplier) {
    this.processCache = processCache;
    this.equipmentCache = equipmentCache;
    this.subEquipmentCache = subEquipmentCache;
    this.containerSupplier = containerSupplier;
  }

  @Override
  public Optional<T> convert(final Tag tag) {
    T map = containerSupplier.get();
    map.put("id", tag.getId());
    map.put("name", tag.getName());
    map.put("description", tag.getDescription());
    map.put("unit", tag.getUnit());
    map.put("mode", tag.getMode());
    map.put("metadata", getMetadata(tag));
    map.put("c2mon", getC2monMetadata(tag));
    return Optional.of(map);
  }

  private T getMetadata(final Tag tag) {
    Metadata metadata = tag.getMetadata();
    if (metadata != null) {
      return metadata.getMetadata().entrySet().stream()
          .filter(entry -> Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue()))
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              Map.Entry::getValue,
              (u, v) -> {
                throw new IllegalStateException(String.format("Duplicate key %s", u));
              },
              containerSupplier
          ));
    }
    return containerSupplier.get();
  }

  protected Map<String, Object> getC2monMetadata(final Tag tag) {
    Map<String, Object> map = new HashMap<>();

    map.put("dataType", tag.getDataType());


    if (!(tag instanceof CommFaultTag) || !tag.getProcessIds().isEmpty()) {
      try {
        Process process = processCache.get(tag.getProcessIds().iterator().next());
        map.put("process", process.getName());
      } catch (Exception e) {
        log.warn("Could not get Process name for tag #{} ({}) from cache. Reason: {}", tag.getId(), tag.getName(), e.getMessage());
      }
    }

    if (!tag.getEquipmentIds().isEmpty()) {
      try {
        Equipment equipment = equipmentCache.get(tag.getEquipmentIds().iterator().next());
        map.put("equipment", equipment.getName());
      } catch (Exception e) {
        log.warn("Could not get Equipment name for tag #{} ({}) from cache. Reason: {}", tag.getId(), tag.getName(), e.getMessage());
      }
    }

    if (!tag.getSubEquipmentIds().isEmpty()) {
      try {
        SubEquipment subEquipment = subEquipmentCache.get(tag.getSubEquipmentIds().iterator().next());
        map.put("subEquipment", subEquipment.getName());
      } catch (Exception e) {
        log.warn("Could not get SubEquipment name for tag #{} ({}) from cache. Reason: {}", tag.getId(), tag.getName(), e.getMessage());
      }
    }

    return map;
  }
}
