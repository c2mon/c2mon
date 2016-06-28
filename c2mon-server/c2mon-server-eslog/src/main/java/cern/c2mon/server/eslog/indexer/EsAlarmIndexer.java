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
package cern.c2mon.server.eslog.indexer;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.eslog.connector.Connector;
import cern.c2mon.server.eslog.structure.mappings.EsAlarmMapping;
import cern.c2mon.server.eslog.structure.types.EsAlarm;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Allows to send the data to ElasticSearch through {@link Connector}.
 *
 * @author Alban Marguet
 */
@Slf4j
@Qualifier("esAlarmIndexer")
@Component
@Data
@EqualsAndHashCode(callSuper = false)
public class EsAlarmIndexer extends EsIndexer {
  /**
   * Holds the ElasticSearch mapping given to an index.
   */
  private final Map<String, String> cacheIndices = new HashMap<>();

  /**
   * Autowired constructor.
   *
   * @param connector handling the connection with ElasticSearch.
   */
  @Autowired
  public EsAlarmIndexer(final Connector connector) {
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
    try {
      if (object != null && object instanceof EsAlarm) {
        logAlarm((EsAlarm) object);
      }
    } catch(ElasticsearchException e) {
      log.debug("EsAlarmIndexer storeData() - Cluster is not reachable");
      throw new IDBPersistenceException();
    }
  }

  @Override
  public void storeData(List data) throws IDBPersistenceException {
    try {
      for (Object object : data) {
        if (object instanceof IFallback) {
          storeData((IFallback) object);
        }
      }
    } catch(ElasticsearchException e) {
      log.debug("EsAlarmIndexer storeData() - Cluster is not reachable");
      throw new IDBPersistenceException();
    }
  }

  /**
   * Ask the {@link Connector} to write the data to ElasticSearch.
   *
   * @param esAlarm to write to the cluster.
   */
  public void logAlarm(EsAlarm esAlarm) {
    if (esAlarm == null) {
      log.debug("logAlarm() - Could not instantiate EsAlarm, null rawValue.");
    }

    String indexName = generateAlarmIndex(esAlarm.getServerTimestamp());
    String mapping = createOrRetrieveMapping(indexName);
    indexData(indexName, mapping, esAlarm);
  }

  /**
   * Format: "alarmPrefix_indexSettings".
   */
  private String generateAlarmIndex(long time) {
    return retrieveIndexFormat(alarmPrefix, time);
  }

  private String createOrRetrieveMapping(String indexName) {
    final String cachedMappings = cacheIndices.get(indexName);

    if (Strings.isNullOrEmpty(cachedMappings)) {
      return new EsAlarmMapping().getMapping();
    }
    return cachedMappings;
  }

  /**
   * Ask the ElasticSearch cluster to retrieve the mappings that it holds for every index/type.
   */
  private void retrieveMappingsFromES() throws IDBPersistenceException {
    Set<String> indicesES = retrieveIndicesFromES();
    for (String index : indicesES) {
      Set<String> types = retrieveTypesFromES(index);
      for (String type : types) {
        MappingMetaData mapping = retrieveMappingES(index, type);

        if (mapping != null) {
          String jsonMapping = mapping.source().toString();
          log.debug("retrieveMappingsFromES() - mapping: " + jsonMapping);
          this.cacheIndices.put(index, jsonMapping);
        }
      }
    }
  }

  /**
   * Send the data to the {@link Connector}: try to index an new entry to ElasticSearch.
   *
   * @param indexName to which add the data.
   * @param mapping   as JSON.
   * @param esAlarm   the data.
   */
  private void indexData(String indexName, String mapping, EsAlarm esAlarm) {
    boolean isAcked = connector.handleAlarmQuery(indexName, mapping, esAlarm);
    if (isAcked) {
      log.debug("logAlarm() - isAcked: " + isAcked);
      cacheIndices.put(indexName, mapping);
    }
  }
}