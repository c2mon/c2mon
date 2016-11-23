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
 * Allows to write a {@link EsSupervisionEvent} to Elasticsearch through the {@link Connector}.
 *
 * @author Alban Marguet
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
@Component("esSupervisionEventIndexer")
public class EsSupervisionEventIndexer<T extends EsSupervisionEvent> extends EsIndexer<T> {

  private final String supervisionMapping = new EsSupervisionMapping().getMapping();

  /**
   * Autowired constructor.
   *
   * @param connector handling the connection to Elasticsearch.
   */
  @Autowired
  public EsSupervisionEventIndexer(final Connector connector, Environment environment) {
    super(connector, environment);
  }

  /**
   * Wait for the connection with Elasticsearch to be alive.
   */
  @Override
  @PostConstruct
  public void init() throws IDBPersistenceException {
    super.init();
  }

  @Override
  public void storeData(T esSupervisionEvent) throws IDBPersistenceException {
    if(esSupervisionEvent == null) {
      return;
    }

    boolean logged = false;
    try {
      String indexName = generateSupervisionIndex(esSupervisionEvent.getTimestamp());
      logged = connector.logSupervisionEvent(indexName, supervisionMapping, esSupervisionEvent);
    } catch (Exception e) {
      log.debug("storeData() - Cluster is not reachable");
      throw new IDBPersistenceException(e);
    }

    if (!logged) {
      throw new IDBPersistenceException("Supervision could not be stored in Elasticsearch");
    }
  }

  @Override
  public void storeData(List<T> data) throws IDBPersistenceException {
    try {
      for (T esSupervisionEvent : data) {
          storeData(esSupervisionEvent);
      }
    } catch(Exception e) {
      log.debug("storeData() - Cluster is not reachable");
      throw new IDBPersistenceException(e);
    }
  }

  /**
   * Format: "supervisionPrefix_index-format".
   */
  private String generateSupervisionIndex(long time) {
    return retrieveIndexFormat(indexPrefix + "-supervision_", time);
  }
}
