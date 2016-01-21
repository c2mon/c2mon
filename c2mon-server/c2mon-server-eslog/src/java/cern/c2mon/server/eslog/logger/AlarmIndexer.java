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

import cern.c2mon.server.eslog.structure.mappings.AlarmMapping;
import cern.c2mon.server.eslog.structure.mappings.Mapping;
import cern.c2mon.server.eslog.structure.types.AlarmES;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows to send the data to ElasticSearch through the Connector interface.
 * @author Alban Marguet
 */
@Slf4j
@Service
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
  public void init() {
    super.init();
  }

  /**
   * Creates a new AlarmQuery to write the data to ElasticSearch inside the alarm index.
   * @param alarmES to write to the cluster.
   */
  public void logAlarm(AlarmES alarmES) {
    if (alarmES != null) {
      String indexName = generateAlarmIndex(alarmES.getServerTimestamp());
      String mapping = createOrRetrieveMapping(indexName);
      boolean isAcked = connector.handleAlarmQuery(indexName, mapping, alarmES);
      if (isAcked) {
        log.debug("logAlarm() - isAcked: " + isAcked);
        indices.put(indexName, mapping);
      }
    }
    else {
      log.debug("logAlarm() - Could not instantiate AlarmES, null value.");
    }
  }

  /**
   * Format: "alarmPrefix_YYYY-MM".
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
      alarmMapping.configure(connector.getShards(), connector.getReplica());
      alarmMapping.setProperties(Mapping.ValueType.alarmType);
      return alarmMapping.getMapping();
    }
  }
}