/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.eslog.structure.converter;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.datatag.DataTag;
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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts the dataTags from the server to the ElasticSearch format {@link AbstractEsTag}.
 *
 * @author Alban Marguet.
 */
@Slf4j
@Component
public class EsTagLogConverter {

  private final ProcessCache processCache;
  private final EquipmentCache equipmentCache;
  private final SubEquipmentCache subEquipmentCache;
  private final Gson gson;

  /**
   * Default ID to return if nothing is found in cache.
   */
  private final long DEFAULT_ID = -1;

  /**
   * Autowired constructor to access cache for processMetadata.
   */
  @Autowired
  public EsTagLogConverter(final ProcessCache processCache, final EquipmentCache equipmentCache, final SubEquipmentCache subEquipmentCache) {
    this.processCache = processCache;
    this.equipmentCache = equipmentCache;
    this.subEquipmentCache = subEquipmentCache;
    this.gson = GsonSupplier.INSTANCE.get();
  }

  /**
   * Converts all the properties of a Tag to create a {@link AbstractEsTag} according to the dataType.
   *
   * @param tag Tag object in C2MON.
   * @return {@link AbstractEsTag}, ready to be logged to the ElasticSearch instance.
   */
  public AbstractEsTag convertToTagES(Tag tag) {
    AbstractEsTag esTagImpl = instantiateTagES(tag.getDataType());

    if(esTagImpl == null) {
      return null;
    }

    setMetadata(tag, esTagImpl);

    esTagImpl.setId(tag.getId());
    esTagImpl.setName(tag.getName());
    esTagImpl.setDataType(tag.getDataType().toLowerCase());

    setSourceAndDaqTimestamp(tag, esTagImpl);

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
   * Handles Metadata of tag and retrieve process, equipment and subEquipment.
   */
  private void setMetadata(Tag tag, AbstractEsTag esTag) {
    retrieveTagMetadata(tag, esTag);
    retrieveParentsMetadata(tag, esTag);
  }

  private void retrieveTagMetadata(Tag tag, AbstractEsTag esTag) {
    Metadata metadata = tag.getMetadata();
    if(metadata == null) {
      return;
    }

    final Map<String, String> esMetadata = metadata.getMetadata().entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().toString()));

    esTag.getMetadata().putAll(esMetadata);
  }

  private void retrieveParentsMetadata(Tag tag, AbstractEsTag esTag) {
    Map<String, String> parentNames = getTagMetadataProcess(tag);
    String process = retrieveProcessIfExists(parentNames);
    String equipment = retrieveEquipmentIfExists(parentNames);
    String subEquipment = retrieveSubEquipmentIfExists(parentNames);

    esTag.setProcess(process);
    esTag.setEquipment(equipment);
    esTag.setSubEquipment(subEquipment);
  }

  private String retrieveProcessIfExists(Map<String, String> parentNames) {
    if(parentNames.containsKey("process")) {
      return parentNames.get("process");
    } else {
      return null;
    }
  }

  private String retrieveEquipmentIfExists(Map<String, String> parentNames) {
    if(parentNames.containsKey("equipment")) {
      return parentNames.get("equipment");
    } else {
      return null;
    }
  }

  private String retrieveSubEquipmentIfExists(Map<String, String> parentNames) {
    if(parentNames.containsKey("subEquipment")) {
      return parentNames.get("subEquipment");
    } else {
      return null;
    }
  }

  private void setSourceAndDaqTimestamp(Tag tag, AbstractEsTag esTag) {
    if(tag instanceof DataTag) {
      Timestamp sourceTimeStamp = ((DataTag) tag).getSourceTimestamp();
      Timestamp daqTimeStamp = ((DataTag) tag).getDaqTimestamp();

      if(sourceTimeStamp != null && daqTimeStamp != null) {
        esTag.setSourceTimestamp(sourceTimeStamp.getTime());
        esTag.setDaqTimestamp(daqTimeStamp.getTime());
      }
    }
  }

  private void setServerTimestamp(Tag tag, AbstractEsTag esTag) {
    Timestamp serverTimeStamp = tag.getCacheTimestamp();
    if(serverTimeStamp != null) {
      esTag.setServerTimestamp(serverTimeStamp.getTime());
    }
  }

  private void setStatus(Tag tag, AbstractEsTag esTag) {
    final DataTagQuality dataTagQuality = tag.getDataTagQuality();
    if(dataTagQuality == null) {
      return;
    }

    int statusCode = dataTagQuality.getInvalidQualityStates().keySet().stream()
            .mapToInt(status -> (int) Math.pow(2, status.getCode()))
            .sum();

    esTag.setStatus(statusCode);
  }

  private void setQuality(Tag tag, AbstractEsTag esTagImpl) {
    DataTagQuality quality = tag.getDataTagQuality();
    if(quality == null) {
      return;
    }

    esTagImpl.setQuality(gson.toJson(quality.getInvalidQualityStates()));
  }

  private void setValid(Tag tag, AbstractEsTag esTagImpl) {
    DataTagQuality quality = tag.getDataTagQuality();

    esTagImpl.setValid(quality == null || quality.getInvalidQualityStates().isEmpty());
  }

  /**
   * Instantiate the right Class of {@link AbstractEsTag according to the dataType: boolean, String or int.
   *
   * @param dataType of the Tag in C2MON.
   * @return {@link AbstractEsTag}: {@link EsTagString}, {@link EsTagNumeric} or {@link EsTagBoolean} for ElasticSearch.
   */
  public AbstractEsTag instantiateTagES(String dataType) {
    dataType = dataType.toLowerCase();

    if(ValueType.isBoolean(dataType)) {
      return new EsTagBoolean();
    } else if(ValueType.isString(dataType)) {
      return new EsTagString();
    } else if(ValueType.isNumeric(dataType)) {
      return new EsTagNumeric();
    } else {
      log.warn("instantiateTagES() - Tag did not correspond to any existing type. (dataType = " + dataType + ").");
      return null;
    }
  }

  /**
   * Retrieve the ProcessName [, EquipmentName, SubEquipmentName] for a given
   * Tag. According to the hierarchy.
   *
   * @param tag for which to get the metadata
   * @return List of names in the order ProcessName [, EquipmentName,
   * SubEquipmentName].
   */
  public Map<String, String> getTagMetadataProcess(Tag tag) {
    Map<String, String> result = new HashMap<>();

    long processId = DEFAULT_ID;
    long equipmentId = DEFAULT_ID;
    long subEquipmentId = DEFAULT_ID;

    boolean subEquipmentIsPresent = !CollectionUtils.isEmpty(tag.getSubEquipmentIds());
    boolean EquipmentIsPresent = !CollectionUtils.isEmpty(tag.getEquipmentIds());
    boolean processIsPresent = !CollectionUtils.isEmpty(tag.getProcessIds());

    if(subEquipmentIsPresent) {
      subEquipmentId = tag.getSubEquipmentIds().iterator().next();
      equipmentId = searchEquipmentInSubEquipmentCache(subEquipmentId);
      processId = searchProcessIdInEquipmentCache(equipmentId);
    } else if(EquipmentIsPresent) {
      equipmentId = tag.getEquipmentIds().iterator().next();
      processId = searchProcessIdInEquipmentCache(equipmentId);
    } else if(processIsPresent) {
      processId = tag.getProcessIds().iterator().next();
    } else {
      log.info("no Process, Equipment or subEquipment");
      return result;
    }

    addMetadataToResult(processId, equipmentId, subEquipmentId, result);

    return result;
  }

  private long searchEquipmentInSubEquipmentCache(long subEquipmentId) {
    SubEquipment subEquipment = subEquipmentCache.get(subEquipmentId);
    if(subEquipment == null) {
      return DEFAULT_ID;
    }

    return subEquipment.getParentId();
  }

  private long searchProcessIdInEquipmentCache(long equipmentId) {
    Equipment equipment = equipmentCache.get(equipmentId);
    if(equipment == null) {
      return DEFAULT_ID;
    }

    return equipment.getProcessId();
  }

  private void addMetadataToResult(long processId, long equipmentId, long subEquipmentId, Map<String, String> result) {
    addProcessNameToResult(processId, result);
    addEquipmentNameToResult(equipmentId, result);
    addSubEquipmentNameToResult(subEquipmentId, result);
  }

  private void addProcessNameToResult(long processId, Map<String, String> result) {
    String processName = getProcessName(processId);
    result.put("process", processName);
  }

  private void addEquipmentNameToResult(long equipmentId, Map<String, String> result) {
    if(equipmentId != -1) {
      String equipmentName = getEquipmentName(equipmentId);
      if(equipmentName != null) {
        result.put("equipment", equipmentName);
      }
    }
  }

  private void addSubEquipmentNameToResult(long subEquipmentId, Map<String, String> result) {
    if(subEquipmentId != -1) {
      String subEquipmentName = getSubEquipmentName(subEquipmentId);
      if(subEquipmentName != null) {
        result.put("subEquipment", subEquipmentName);
      }
    }
  }

  /**
   * Retrieve the name of a Process according to the id.
   *
   * @param processId of the tag.
   * @return processName
   */
  public String getProcessName(long processId) {
    String processName = null;

    Process process = processCache.get(processId);
    if(process != null) {
      processName = process.getName();
    }

    return processName;
  }

  /**
   * Retrieve the name of an Equipment according to the id.
   *
   * @param equipmentId of the tag.
   * @return equipmentName
   */
  public String getEquipmentName(long equipmentId) {
    String equipmentName = null;

    Equipment equipment = equipmentCache.get(equipmentId);
    if(equipment != null) {
      equipmentName = equipment.getName();
    }

    return equipmentName;
  }

  /**
   * Retrieve the Name of a SubEquipment according to the id.
   *
   * @param subEquipmentId of the tag.
   * @return subEquipmentName
   */
  public String getSubEquipmentName(long subEquipmentId) {
    SubEquipment subEquipment = subEquipmentCache.get(subEquipmentId);
    String subEquipmentName = null;
    if(subEquipment != null) {
      subEquipmentName = subEquipment.getName();
    }

    return subEquipmentName;
  }
}