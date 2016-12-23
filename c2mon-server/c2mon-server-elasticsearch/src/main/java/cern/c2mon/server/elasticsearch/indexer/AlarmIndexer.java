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

import java.util.Collections;
import java.util.List;

import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.server.elasticsearch.connector.TransportConnector;
import cern.c2mon.server.elasticsearch.structure.mappings.MappingFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.elasticsearch.structure.types.EsAlarm;

/**
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 */
@Slf4j
@Component
public class AlarmIndexer implements IDBPersistenceHandler<EsAlarm> {

  private TransportConnector connector;

  @Autowired
  public AlarmIndexer(final TransportConnector connector) {
    this.connector = connector;
  }

  @Override
  public void storeData(EsAlarm alarm) throws IDBPersistenceException {
    storeData(Collections.singletonList(alarm));
  }

  @Override
  public void storeData(List<EsAlarm> alarms) throws IDBPersistenceException {
    try {
      long failed = alarms.stream().filter(alarm -> !this.indexAlarm(alarm)).count();

      if (failed > 0) {
        throw new IDBPersistenceException("Failed to index " + failed + " of " + alarms.size() + " alarms");
      }
    } catch (Exception e) {
      throw new IDBPersistenceException(e);
    }
  }

  private boolean indexAlarm(EsAlarm alarm) {
    String indexName = getOrCreateIndex(alarm);

    log.debug("Adding new alarm event to index {}", indexName);
    return connector.getClient().prepareIndex().setIndex(indexName)
        .setType("alarm")
        .setSource(alarm.toString())
        .setRouting(alarm.getId())
        .get().isCreated();
  }

  private String getOrCreateIndex(EsAlarm alarm) {
    String index = Indices.indexFor(alarm);

    if (!Indices.exists(index)) {
      Indices.create(index, "alarm", MappingFactory.createAlarmMapping());
    }

    return index;
  }

  @Override
  public String getDBInfo() {
    return "elasticsearch/alarm";
  }
}
