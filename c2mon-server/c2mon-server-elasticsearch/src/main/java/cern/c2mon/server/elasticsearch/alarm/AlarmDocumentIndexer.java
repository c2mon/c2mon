/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.elasticsearch.alarm;

import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.elasticsearch.IndexManager;
import cern.c2mon.server.elasticsearch.IndexNameManager;
import cern.c2mon.server.elasticsearch.MappingFactory;

/**
 * This class manages the fallback-aware indexing of {@link AlarmDocument}
 * instances to the Elasticsearch cluster.
 *
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 * @author Serhiy Boychenko
 */
@Slf4j
@Component
public class AlarmDocumentIndexer implements IDBPersistenceHandler<AlarmDocument> {

  @Autowired
  private IndexNameManager indexNameManager;

  @Autowired
  private IndexManager indexManager;

  @Override
  public void storeData(AlarmDocument alarm) throws IDBPersistenceException {
    storeData(Collections.singletonList(alarm));
  }

  @Override
  public void storeData(List<AlarmDocument> alarms) throws IDBPersistenceException {
    try {
      long failed = alarms.stream().filter(alarm -> !this.indexAlarm(alarm)).count();

      if (failed > 0) {
        throw new IDBPersistenceException("Failed to index " + failed + " of " + alarms.size() + " alarms");
      }
    } catch (Exception e) {
      throw new IDBPersistenceException(e);
    }
  }

  private boolean indexAlarm(AlarmDocument alarm) {
    String indexName = getOrCreateIndex(alarm);

    log.debug("Indexing alarm #{} to index {}", alarm.getId(), indexName);

    return indexManager.index(indexName, alarm.toString(), alarm.getId());
  }

  private String getOrCreateIndex(AlarmDocument alarm) {
    String index = indexNameManager.indexFor(alarm);

    if (!indexManager.exists(index)) {
      indexManager.create(index, MappingFactory.createAlarmMapping());
    }

    return index;

  }

  @Override
  public String getDBInfo() {
    return "elasticsearch/alarm";
  }
}
