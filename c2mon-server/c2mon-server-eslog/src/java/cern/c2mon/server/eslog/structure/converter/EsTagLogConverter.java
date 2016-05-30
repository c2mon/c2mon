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
package cern.c2mon.server.eslog.structure.converter;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.eslog.structure.mappings.EsMapping.ValueType;
import cern.c2mon.server.eslog.structure.types.*;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.metadata.Metadata;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Converts the dataTags from the server to the ElasticSearch format {@link AbstractEsTag}.
 *
 * @author Alban Marguet.
 */
@Slf4j
@Component
public class EsTagLogConverter {

  /**
   * Default ID to return if nothing is found in cache.
   */
  private static final long DEFAULT_ID = -1;

  private final Gson gson = GsonSupplier.INSTANCE.get();

  private final ProcessCache processCache;
  private final EquipmentCache equipmentCache;
  private final SubEquipmentCache subEquipmentCache;

  @Autowired
  public EsTagLogConverter(final ProcessCache processCache,
                           final EquipmentCache equipmentCache,
                           final SubEquipmentCache subEquipmentCache) {
    this.processCache = processCache;
    this.equipmentCache = equipmentCache;
    this.subEquipmentCache = subEquipmentCache;
  }

  /**
   * Converts all the properties of a Tag to create a {@link AbstractEsTag} according to the dataType.
   *
   * @param tag Tag object in C2MON.
   * @return {@link AbstractEsTag}, ready to be logged to the ElasticSearch instance.
   */
  public AbstractEsTag convertToTagES(final Tag tag) {
    AbstractEsTag esTagImpl = instantiateTagES(tag.getDataType());
    if (esTagImpl == null) {
      return null;
    }

    setMetadata(tag, esTagImpl);

    esTagImpl.setId(tag.getId());
    esTagImpl.setName(tag.getName());
    esTagImpl.setDataType(tag.getDataType().toLowerCase());

    setSourceTimestamp(tag, esTagImpl);
    setDaqTimestamp(tag, esTagImpl);

    setServerTimestamp(tag, esTagImpl);
    setStatus(tag, esTagImpl);
    setQuality(tag, esTagImpl);
    setValid(tag, esTagImpl);

    esTagImpl.setValue(tag.getValue());
    esTagImpl.setValueDescription(tag.getValueDescription());

    log.trace("convertToTagES() - new esTagImpl: " + esTagImpl.toString());
    return esTagImpl;
  }

  /**
   * Instantiate the right Class of {@link AbstractEsTag according to the dataType: boolean, String or int.
   *
   * @param dataType of the Tag in C2MON.
   * @return {@link AbstractEsTag}: {@link EsTagString}, {@link EsTagNumeric} or {@link EsTagBoolean} for ElasticSearch.
   */
  public AbstractEsTag instantiateTagES(String dataType) {
    dataType = dataType.toLowerCase();

    if (ValueType.isBoolean(dataType)) {
      return new EsTagBoolean();
    } else if (ValueType.isString(dataType)) {
      return new EsTagString();
    } else if (ValueType.isNumeric(dataType)) {
      return new EsTagNumeric();
    } else {
      log.warn("instantiateTagES() - Tag did not correspond to any existing type. (dataType = " + dataType + ").");
      return null;
    }
  }

  /**
   * Handles Metadata of tag and retrieve process, equipment and subEquipment.
   */
  private void setMetadata(final Tag tag, final AbstractEsTag esTag) {
    esTag.getMetadata().putAll(retrieveTagMetadata(tag));

    Map<String, String> parentNames = getTagProcessMetadata(tag);
    esTag.setProcess(parentNames.get("process"));
    esTag.setEquipment(parentNames.get("equipment"));
    esTag.setSubEquipment(parentNames.get("subEquipment"));
  }

  private Map<String, String> retrieveTagMetadata(Tag tag) {
    Metadata metadata = tag.getMetadata();
    if (metadata == null) {
      return Collections.emptyMap();
    }
    return metadata.getMetadata().entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().toString()));
  }

  private void setSourceTimestamp(Tag tag, AbstractEsTag esTag) {
    if (!(tag instanceof DataTag)) {
      return;
    }

    Optional.ofNullable(((DataTag) tag).getSourceTimestamp())
            .map(Timestamp::getTime)
            .ifPresent(esTag::setSourceTimestamp);
  }

  private void setDaqTimestamp(Tag tag, AbstractEsTag esTag) {
    if (!(tag instanceof DataTag)) {
      return;
    }

    Optional.ofNullable(((DataTag) tag).getDaqTimestamp())
            .map(Timestamp::getTime)
            .ifPresent(esTag::setDaqTimestamp);
  }

  private void setServerTimestamp(Tag tag, AbstractEsTag esTag) {
    Optional.ofNullable(tag.getCacheTimestamp())
            .map(Timestamp::getTime)
            .ifPresent(esTag::setServerTimestamp);
  }

  private void setStatus(Tag tag, AbstractEsTag esTag) {
    Optional.ofNullable(tag.getDataTagQuality())
            .map(dataTagQuality -> dataTagQuality.getInvalidQualityStates().keySet())
            .map(tagQualityStatuses -> tagQualityStatuses.stream()
                    .mapToInt(status -> (int) Math.pow(2, status.getCode()))
                    .sum())
            .ifPresent(esTag::setStatus);
  }

  private void setQuality(Tag tag, AbstractEsTag esTagImpl) {
    Optional.ofNullable(tag.getDataTagQuality())
            .map(DataTagQuality::getInvalidQualityStates)
            .map(gson::toJson)
            .ifPresent(esTagImpl::setQuality);
  }

  private void setValid(Tag tag, AbstractEsTag esTagImpl) {
    DataTagQuality quality = tag.getDataTagQuality();

    esTagImpl.setValid(quality == null || quality.getInvalidQualityStates().isEmpty());
  }

  /**
   * Retrieve the ProcessName [, EquipmentName, SubEquipmentName] for a given
   * Tag. According to the hierarchy.
   *
   * @param tag for which to get the metadata
   * @return List of names in the order ProcessName [, EquipmentName,
   * SubEquipmentName].
   */
  public Map<String, String> getTagProcessMetadata(Tag tag) {

    long equipmentId = DEFAULT_ID;
    long subEquipmentId = DEFAULT_ID;
    long processId = DEFAULT_ID;

    boolean subEquipmentIsPresent = !CollectionUtils.isEmpty(tag.getSubEquipmentIds());
    boolean EquipmentIsPresent = !CollectionUtils.isEmpty(tag.getEquipmentIds());
    boolean processIsPresent = !CollectionUtils.isEmpty(tag.getProcessIds());

    if (subEquipmentIsPresent) {
      subEquipmentId = tag.getSubEquipmentIds().iterator().next();
      equipmentId = searchEquipmentInSubEquipmentCache(subEquipmentId);
      processId = searchProcessIdInEquipmentCache(equipmentId);

      return getMetadata(processId, equipmentId, subEquipmentId);
    }

    if (EquipmentIsPresent) {
      equipmentId = tag.getEquipmentIds().iterator().next();
      processId = searchProcessIdInEquipmentCache(equipmentId);

      return getMetadata(processId, equipmentId, subEquipmentId);
    }

    if (processIsPresent) {
      processId = tag.getProcessIds().iterator().next();

      return getMetadata(processId, equipmentId, subEquipmentId);
    }

    log.info("no Process, Equipment or subEquipment");
    return Collections.emptyMap();
  }

  private long searchEquipmentInSubEquipmentCache(long subEquipmentId) {
    return Optional.ofNullable(subEquipmentCache.get(subEquipmentId))
            .map(SubEquipment::getParentId)
            .orElse(DEFAULT_ID);
  }

  private long searchProcessIdInEquipmentCache(long equipmentId) {
    return Optional.ofNullable(equipmentCache.get(equipmentId))
            .map(Equipment::getProcessId)
            .orElse(DEFAULT_ID);
  }

  private Map<String, String> getMetadata(long processId, long equipmentId, long subEquipmentId) {
    final Map<String, String> metadata = new HashMap<>();

    extractProcessName(processId)
            .ifPresent(processName -> metadata.put("process", processName));
    extractEquipmentName(equipmentId)
            .ifPresent(equipmentName -> metadata.put("equipment", equipmentName));
    extractSubEquipmentName(subEquipmentId)
            .ifPresent(subEquipmentName -> metadata.put("subEquipment", subEquipmentName));

    return metadata;
  }

  private Optional<String> extractProcessName(long processId) {
    return Optional.ofNullable(processCache.get(processId))
            .map(Process::getName);
  }

  private Optional<String> extractEquipmentName(long equipmentId) {
    if (equipmentId == -1) {
      return Optional.empty();
    }
    return Optional.ofNullable(equipmentCache.get(equipmentId))
            .map(AbstractEquipment::getName);
  }

  private Optional<String> extractSubEquipmentName(long subEquipmentId) {
    if (subEquipmentId == -1) {
      return Optional.empty();
    }
    return Optional.ofNullable(subEquipmentCache.get(subEquipmentId))
            .map(AbstractEquipment::getName);
  }
}