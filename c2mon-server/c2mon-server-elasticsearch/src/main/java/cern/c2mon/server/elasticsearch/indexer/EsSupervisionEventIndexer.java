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
package cern.c2mon.server.elasticsearch.indexer;

import java.util.List;

import javax.annotation.PostConstruct;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.connector.Connector;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.elasticsearch.structure.mappings.EsSupervisionMapping;
import cern.c2mon.server.elasticsearch.structure.types.EsSupervisionEvent;

/**
 * @author Alban Marguet
 */
@Slf4j
@Component("esSupervisionEventIndexer")
public class EsSupervisionEventIndexer<T extends EsSupervisionEvent> extends EsIndexer<T> {

  private final String supervisionMapping = new EsSupervisionMapping().getMapping();

  @Autowired
  public EsSupervisionEventIndexer(final Connector connector, final ElasticsearchProperties properties) {
    super(connector, properties);
  }

  @Override
  @PostConstruct
  public void init() throws IDBPersistenceException {
    super.init();
  }

  @Override
  public void storeData(T esSupervisionEvent) throws IDBPersistenceException {
    if (esSupervisionEvent == null) {
      return;
    }

    boolean logged = false;
    try {
      String indexName = generateSupervisionIndex(esSupervisionEvent.getTimestamp());
      logged = connector.logSupervisionEvent(indexName, supervisionMapping, esSupervisionEvent);
    } catch (Exception e) {
      log.debug("Cluster is not reachable!");
      throw new IDBPersistenceException(e);
    }

    if (!logged) {
      throw new IDBPersistenceException("Supervision event could not be stored in Elasticsearch");
    }
  }

  @Override
  public void storeData(List<T> data) throws IDBPersistenceException {
    try {
      for (T esSupervisionEvent : data) {
          storeData(esSupervisionEvent);
      }
    } catch(Exception e) {
      log.debug("Cluster is not reachable!");
      throw new IDBPersistenceException(e);
    }
  }

  private String generateSupervisionIndex(long time) {
    return retrieveIndexFormat(properties.getIndexPrefix() + "-supervision_", time);
  }
}
