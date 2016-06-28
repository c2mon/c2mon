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
package cern.c2mon.server.eslog.config;

import cern.c2mon.pmanager.IAlarmListener;
import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.pmanager.persistence.impl.TimPersistenceManager;
import cern.c2mon.server.eslog.alarm.DummyEsAlarmListener;
import cern.c2mon.server.eslog.structure.types.EsAlarm;
import cern.c2mon.server.eslog.structure.types.EsSupervisionEvent;
import cern.c2mon.server.eslog.structure.types.tag.EsTagNumeric;
import cern.c2mon.server.eslog.structure.types.tag.EsTagString;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * This class is responsible for configuring the Spring context for the eslog
 * module. It is automatically detected.
 *
 * @author Alban Marguet.
 */
@Configuration
@ComponentScan(basePackages = "cern.c2mon.server.eslog")
public class EsLogConfiguration {

  @Bean
  public IAlarmListener alarmListener() {
    return new DummyEsAlarmListener();
  }


  @Bean(name = "esSupervisionEventPersistenceManager")
  public IPersistenceManager esSupervisionEventPersistenceManager(
      @Qualifier("esSupervisionEventIndexer") final IDBPersistenceHandler persistenceHandler,
      @Value("${c2mon.server.home}/log/supervisionESfallback.txt") final String fallbackFile,
      final IAlarmListener alarmListener) {

    return new TimPersistenceManager(persistenceHandler, fallbackFile, alarmListener, new EsSupervisionEvent());
  }

  @Bean(name = "esAlarmPersistenceManager")
  public IPersistenceManager esAlarmPersistanceManger(
      @Qualifier("esAlarmIndexer") final IDBPersistenceHandler persistenceHandler,
      @Value("${c2mon.server.home}/log/alarmESfallback.txt") final String fallbackFile,
      final IAlarmListener alarmListener) {

    return new TimPersistenceManager(persistenceHandler, fallbackFile, alarmListener, new EsAlarm());
  }


  @Bean(name = "esTagNumericPersistenceManager")
  public IPersistenceManager esTagNumericPersistenceManager(
      @Qualifier("esTagIndexer") final IDBPersistenceHandler persistenceHandler,
      @Value("${c2mon.server.home}/log/tagNumericESfallback.txt") final String fallbackFile,
      final IAlarmListener alarmListener) {

    return new TimPersistenceManager(persistenceHandler, fallbackFile, alarmListener, new EsTagNumeric());
  }

  @Bean(name = "esTagStringPersistenceManager")
  public IPersistenceManager esTagStringPersistenceManager(
      @Qualifier("esTagIndexer") final IDBPersistenceHandler persistenceHandler,
      @Value("${c2mon.server.home}/log/tagStringESfallback.txt") final String fallbackFile,
      final IAlarmListener alarmListener) {

    return new TimPersistenceManager(persistenceHandler, fallbackFile, alarmListener, new EsTagString());
  }

  @Bean(name = "esTagBooleanPersistenceManager")
  public IPersistenceManager esTagBooleanPersistenceManager(
      @Qualifier("esTagIndexer") final IDBPersistenceHandler persistenceHandler,
      @Value("${c2mon.server.home}/log/tagBooleanESfallback.txt") final String fallbackFile,
      final IAlarmListener alarmListener) {

    return new TimPersistenceManager(persistenceHandler, fallbackFile, alarmListener, new EsTagString());
  }

}
