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

import java.util.List;

import javax.annotation.PostConstruct;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.eslog.connector.Connector;
import cern.c2mon.server.eslog.structure.mappings.EsAlarmMapping;
import cern.c2mon.server.eslog.structure.types.EsAlarm;

/**
 * Allows to send the data to ElasticSearch through {@link Connector}.
 *
 * @author Alban Marguet
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
@Component("esAlarmIndexer")
public class EsAlarmIndexer<T extends EsAlarm> extends EsIndexer<T> {

  private final String alarmMapping = new EsAlarmMapping().getMapping();

  /**
   * Autowired constructor.
   *
   * @param connector
   *          handling the connection with ElasticSearch.
   */
  @Autowired
  public EsAlarmIndexer(final Connector connector) {
    super(connector);
  }

  /**
   * Make sure the connection is alive.
   */
  @Override
  @PostConstruct
  public void init() throws IDBPersistenceException {
    super.init();
  }

  @Override
  public void storeData(T esAlarm) throws IDBPersistenceException {
    if (esAlarm == null) {
      return;
    }

    boolean logged = false;
    try {
        String indexName = generateAlarmIndex(esAlarm.getTimestamp());
        logged = connector.logAlarmEvent(indexName, alarmMapping, esAlarm);
    } catch (Exception e) {
      log.debug("storeData() - Cluster is not reachable");
      throw new IDBPersistenceException(e);
    }

    if (!logged) {
      throw new IDBPersistenceException("Alarm could not be stored in Elasticsearch");
    }
  }

  @Override
  public void storeData(List<T> data) throws IDBPersistenceException {
    try {
      for (T esAlarm : data) {
        storeData(esAlarm);
      }
    } catch (Exception e) {
      log.debug("storeData() - Cluster is not reachable");
      throw new IDBPersistenceException(e);
    }
  }

  /**
   * Format: "alarmPrefix_indexSettings".
   */
  private String generateAlarmIndex(long time) {
    return retrieveIndexFormat(indexPrefix + "-alarm_", time);
  }
}