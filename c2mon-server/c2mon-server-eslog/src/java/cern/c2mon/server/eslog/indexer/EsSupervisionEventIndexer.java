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
import cern.c2mon.server.eslog.structure.mappings.EsSupervisionMapping;
import cern.c2mon.server.eslog.structure.types.EsSupervisionEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Allows to write a {@link EsSupervisionEvent} to ElasticSearch through the {@link Connector}.
 *
 * @author Alban Marguet
 */
@Service
@Qualifier("esSupervisionEventIndexer")
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class EsSupervisionEventIndexer extends EsIndexer {
  /**
   * Holds the ElasticSearch mapping given to an index.
   */
  Map<String, String> cacheIndices = new HashMap<>();

  /**
   * Autowired constructor.
   *
   * @param connector handling the connection to ElasticSearch.
   */
  @Autowired
  public EsSupervisionEventIndexer(final Connector connector) {
    super(connector);
  }

  /**
   * Wait for the connection with ElasticSearch to be alive.
   */
  @PostConstruct
  public void init() throws IDBPersistenceException {
    super.init();
    retrieveMappingsFromES();
  }

  @Override
  public void storeData(IFallback object) throws IDBPersistenceException {
    if(object == null) {
      return;
    }
    try {
      if (object instanceof EsSupervisionEvent) {
        logSupervisionEvent((EsSupervisionEvent) object);
      }
    } catch(ElasticsearchException e) {
      throw new IDBPersistenceException();
    }
  }

  @Override
  public void storeData(List data) throws IDBPersistenceException {
    if(CollectionUtils.isEmpty(data)) {
      return;
    }
    try {
      for (Object object : data) {
        if (object instanceof IFallback) {
          storeData((IFallback) object);
        }
      }
    } catch(ElasticsearchException e) {
      throw new IDBPersistenceException();
    }
  }

  /**
   * Write the {@link EsSupervisionEvent} to ElasticSearch.
   *
   * @param esSupervisionEvent to be written to ElasticSearch.
   */
  public void logSupervisionEvent(EsSupervisionEvent esSupervisionEvent) {
    if (esSupervisionEvent != null) {
      String indexName = generateSupervisionIndex(esSupervisionEvent.getEventTime());
      String mapping = createMappingIfNewIndex(indexName);
      indexData(indexName, mapping, esSupervisionEvent);
    } else {
      log.debug("logSupervisionEvent() - Could not instantiate SupervisionEventImpl, null rawValue.");
    }
  }

  /**
   * Format: "supervisionPrefix_index-format".
   */
  private String generateSupervisionIndex(long time) {
    return retrieveIndexFormat(supervisionPrefix, time);
  }

  private String createMappingIfNewIndex(String indexName) {
    if (cacheIndices.keySet().contains(indexName)) {
      return cacheIndices.get(indexName);
    } else {
      EsSupervisionMapping supervisionMapping = new EsSupervisionMapping();
      return supervisionMapping.getMapping();
    }
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
   * Send the data to the Connector: try to index an new entry to ElasticSearch.
   *
   * @param indexName          to which add the data.
   * @param mapping            as JSON.
   * @param esSupervisionEvent the data.
   */
  private void indexData(String indexName, String mapping, EsSupervisionEvent esSupervisionEvent) {
    boolean isAcked = connector.handleSupervisionQuery(indexName, mapping, esSupervisionEvent);
    if (isAcked) {
      log.debug("logSupervisionEvent() - isAcked: " + isAcked);
      cacheIndices.put(indexName, mapping);
    }
  }
}