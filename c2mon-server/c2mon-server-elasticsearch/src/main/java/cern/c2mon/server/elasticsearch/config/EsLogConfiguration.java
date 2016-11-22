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
package cern.c2mon.server.elasticsearch.config;

import cern.c2mon.server.elasticsearch.structure.types.tag.EsTag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.pmanager.IAlarmListener;
import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.pmanager.persistence.impl.PersistenceManager;
import cern.c2mon.server.elasticsearch.structure.types.EsAlarm;
import cern.c2mon.server.elasticsearch.structure.types.EsSupervisionEvent;

/**
 * This class is responsible for configuring the Spring context for
 * the elasticsearch module.
 *
 * @author Alban Marguet.
 */
@Configuration
@ComponentScan(basePackages = "cern.c2mon.server.elasticsearch")
public class EsLogConfiguration {

  @Bean(name = "esSupervisionEventPersistenceManager")
  public IPersistenceManager<EsSupervisionEvent> esSupervisionEventPersistenceManager(
      @Qualifier("esSupervisionEventIndexer") final IDBPersistenceHandler<EsSupervisionEvent> persistenceHandler,
      @Value("/tmp/supervisionESfallback.txt") final String fallbackFile,
      @Qualifier("esAlarmListener") final IAlarmListener alarmListener) {

    return new PersistenceManager<EsSupervisionEvent>(persistenceHandler, fallbackFile, alarmListener, new EsSupervisionEvent());
  }

  @Bean(name = "esAlarmPersistenceManager")
  public IPersistenceManager<EsAlarm> esAlarmPersistanceManger(
      @Qualifier("esAlarmIndexer") final IDBPersistenceHandler<EsAlarm> persistenceHandler,
      @Value("/tmp/alarmESfallback.txt") final String fallbackFile,
      @Qualifier("esAlarmListener") final IAlarmListener alarmListener) {

    return new PersistenceManager<EsAlarm>(persistenceHandler, fallbackFile, alarmListener, new EsAlarm());
  }


  @Bean(name = "esTagPersistenceManager")
  public IPersistenceManager<EsTag> esTagPersistenceManager(
      @Qualifier("esTagIndexer") final IDBPersistenceHandler<EsTag> persistenceHandler,
      @Value("/tmp/tagESfallback.txt") final String fallbackFile,
      @Qualifier("esAlarmListener") final IAlarmListener alarmListener) {

    return new PersistenceManager<EsTag>(persistenceHandler, fallbackFile, alarmListener, new EsTag());
  }
}
