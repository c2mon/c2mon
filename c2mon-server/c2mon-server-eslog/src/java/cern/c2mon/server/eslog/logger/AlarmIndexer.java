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
package cern.c2mon.server.eslog.logger;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.eslog.structure.mappings.AlarmMapping;
import cern.c2mon.server.eslog.structure.mappings.Mapping;
import cern.c2mon.server.eslog.structure.queries.ClusterNotAvailableException;
import cern.c2mon.server.eslog.structure.queries.QueryIndices;
import cern.c2mon.server.eslog.structure.queries.QueryTypes;
import cern.c2mon.server.eslog.structure.types.AlarmES;
import cern.c2mon.server.eslog.structure.types.SupervisionES;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Allows to send the data to ElasticSearch through the Connector interface.
 * @author Alban Marguet
 */
@Slf4j
@Service
@Qualifier("alarmIndexer")
@Data
@EqualsAndHashCode(callSuper = false)
public class AlarmIndexer extends Indexer {
  Map<String, String> indices = new HashMap<>();

  /** Autowired constructor */
  @Autowired
  public AlarmIndexer(final Connector connector) {
    super(connector);
  }

  /**
   * Make sure the connection is alive.
   */
  @PostConstruct
  public void init() throws IDBPersistenceException {
    super.init();
    retrieveMappingsFromES();
  }

  @Override
  public void storeData(IFallback object) throws IDBPersistenceException {
    if (object != null && object instanceof AlarmES) {
      logAlarm((AlarmES) object);
    }
  }

  @Override
  public void storeData(List data) throws IDBPersistenceException {
    for (Object object : data) {
      if (object instanceof IFallback) {
        storeData((IFallback) object);
      }
    }
  }

  /**
   * Creates a new AlarmQuery to write the data to ElasticSearch inside the alarm index.
   * @param alarmES to write to the cluster.
   */
  public void logAlarm(AlarmES alarmES) throws IDBPersistenceException {
    if (alarmES != null) {
      String indexName = generateAlarmIndex(alarmES.getServerTimestamp());
      String mapping = createOrRetrieveMapping(indexName);
      indexData(indexName, mapping, alarmES);
    }
    else {
      log.debug("logAlarm() - Could not instantiate AlarmES, null value.");
    }
  }

  /**
   * Format: "alarmPrefix_indexSettings".
   */
  public String generateAlarmIndex(long time) {
    return retrieveIndexFormat(alarmPrefix, time);
  }

  public String createOrRetrieveMapping(String indexName) {
    if (indices.keySet().contains(indexName)) {
      return indices.get(indexName);
    }
    else {
      AlarmMapping alarmMapping = new AlarmMapping();
      return alarmMapping.getMapping();
    }
  }

  public void retrieveMappingsFromES() throws IDBPersistenceException {
    List<String> indicesES = retrieveIndicesFromES();
    for (String index : indicesES) {
      List<String> types = retrieveTypesFromES(index);
      for (String type : types) {
        MappingMetaData mapping = retrieveMappingES(index, type);

        if (mapping != null) {
          String jsonMapping = mapping.source().toString();
          log.debug("retrieveMappingsFromES() - mapping: " + jsonMapping);
          this.indices.put(index, jsonMapping);
        }
      }
    }
  }

  public void indexData(String indexName, String mapping, AlarmES alarmES) throws IDBPersistenceException {
    boolean isAcked = connector.handleAlarmQuery(indexName, mapping, alarmES);
    if (isAcked) {
      log.debug("logAlarm() - isAcked: " + isAcked);
      indices.put(indexName, mapping);
    }
  }
}