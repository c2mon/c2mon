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
package cern.c2mon.server.elasticsearch.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import cern.c2mon.pmanager.IAlarmListener;
import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.pmanager.persistence.impl.PersistenceManager;
import cern.c2mon.server.elasticsearch.alarm.AlarmDocument;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocument;
import cern.c2mon.server.elasticsearch.tag.TagDocument;

/**
 * Beans needed for fallback-aware persistence.
 *
 * @author Justin Lewis Salmon
 */
public class ElasticsearchPersistenceConfig {

  @Autowired
  private ElasticsearchProperties properties;

  @Bean
  public IPersistenceManager<TagDocument> tagDocumentPersistenceManager(
      final IDBPersistenceHandler<TagDocument> esTagIndexer, final IAlarmListener fallbackActivationListener) {
    String fallbackFile = properties.getTagFallbackFile();
    return new PersistenceManager<>(esTagIndexer, fallbackFile, fallbackActivationListener, new TagDocument());
  }

  @Bean
  public IPersistenceManager<AlarmDocument> alarmDocumentPersistenceManager(
      final IDBPersistenceHandler<AlarmDocument> esAlarmIndexer, final IAlarmListener fallbackActivationListener) {
    String fallbackFile = properties.getAlarmFallbackFile();
    return new PersistenceManager<>(esAlarmIndexer, fallbackFile, fallbackActivationListener, new AlarmDocument());
  }

  @Bean
  public IPersistenceManager<SupervisionEventDocument> supervisionEventDocumentPersistenceManager(
      final IDBPersistenceHandler<SupervisionEventDocument> esSupervisionEventIndexer, final IAlarmListener fallbackActivationListener) {
    String fallbackFile = properties.getSupervisionFallbackFile();
    return new PersistenceManager<>(esSupervisionEventIndexer, fallbackFile, fallbackActivationListener, new SupervisionEventDocument());
  }
}
