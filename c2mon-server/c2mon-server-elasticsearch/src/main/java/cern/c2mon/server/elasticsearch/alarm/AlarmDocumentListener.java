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

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.shared.common.CacheEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import javax.annotation.PostConstruct;

/**
 * Listens for {@link Alarm} updates and converts them to {@link AlarmDocument}
 * instances before sending them to the {@link IPersistenceManager} responsible
 * for indexing them.
 *
 * @author Alban Marguet
 */
@Slf4j
@Component
public class AlarmDocumentListener {

  private final ElasticsearchProperties properties;

  private final C2monCache<Alarm> alarmCache;

  @Qualifier("alarmDocumentPersistenceManager")
  private final IPersistenceManager<AlarmDocument> persistenceManager;

  private final AlarmValueDocumentConverter converter;

  @Autowired
  public AlarmDocumentListener(ElasticsearchProperties properties, final C2monCache<Alarm> alarmCache, IPersistenceManager<AlarmDocument> persistenceManager, AlarmValueDocumentConverter converter) {
    this.properties = properties;
    this.alarmCache = alarmCache;
    this.persistenceManager = persistenceManager;
    this.converter = converter;
  }

  @PostConstruct
  public void init() {
    if (properties.isEnabled()) {
      alarmCache.getCacheListenerManager().registerListener(alarm -> {
        if (alarm != null) {
          persistenceManager.storeData(converter.convert(alarm));
        } else {
          log.warn("Received a null alarm");
        }
      }, CacheEvent.UPDATE_ACCEPTED);
    }
  }
}
