/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.eslog.logger;

import cern.c2mon.server.eslog.structure.mappings.Mapping;
import cern.c2mon.server.eslog.structure.mappings.SupervisionMapping;
import cern.c2mon.server.eslog.structure.queries.ClusterNotAvailableException;
import cern.c2mon.server.eslog.structure.queries.QueryIndices;
import cern.c2mon.server.eslog.structure.queries.QueryTypes;
import cern.c2mon.server.eslog.structure.types.SupervisionES;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allows to write a SupervisionEvent to ElasticSearch through the Connector.
 * @author Alban Marguet
 */
@Service
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class SupervisionIndexer extends Indexer {
  Map<String, String> indices = new HashMap<>();
  @Autowired
  public SupervisionIndexer(final Connector connector) {
    super(connector);
  }

  /**
   * Wait for the connection with ElasticSearch to be alive.
   */
  @PostConstruct
  public void init() {
    super.init();
    retrieveMappingsFromES();
  }

  /**
   * Write the SupervisionES to ElasticSearch in the Supervision index.
   * @param supervisionES to be written to ElasticSearch.
   */
  public void logSupervisionEvent(SupervisionES supervisionES) {
    if (supervisionES != null) {
      String indexName = generateSupervisionIndex(supervisionES.getEventTime());
      String mapping = createMappingIfNewIndex(indexName);
      indexData(indexName, mapping, supervisionES);
    }
    else {
      log.debug("logSupervisionEvent() - Could not instantiate SupervisionEventImpl, null value.");
    }
  }

  /**
   * Format: "supervisionPrefix_YYYY-MM".
   */
  public String generateSupervisionIndex(long time) {
    return retrieveIndexFormat(supervisionPrefix, time);
  }

  public String createMappingIfNewIndex(String indexName) {
    if (indices.keySet().contains(indexName)) {
      return indices.get(indexName);
    }
    else {
      SupervisionMapping supervisionMapping = new SupervisionMapping();
      supervisionMapping.setProperties(Mapping.ValueType.supervisionType);
      return supervisionMapping.getMapping();
    }
  }

  public void retrieveMappingsFromES() {
    List<String> indicesES = retrieveIndicesFromES();
    for (String index : indicesES) {
      List<String> types = retrieveTypesFromES(index);
      for (String type : types) {
        MappingMetaData mapping = retrieveMappingES(index, type);
        String jsonMapping = mapping.source().toString();
        log.debug("retrieveMappingsFromES() - mapping: " + jsonMapping);
        this.indices.put(index, jsonMapping);
      }
    }
  }

  public void indexData(String indexName, String mapping, SupervisionES supervisionES) {
    boolean isAcked = connector.handleSupervisionQuery(indexName, mapping, supervisionES);
    if (isAcked) {
      log.debug("logSupervisionEvent() - isAcked: " + isAcked);
      indices.put(indexName, mapping);
    }
  }
}