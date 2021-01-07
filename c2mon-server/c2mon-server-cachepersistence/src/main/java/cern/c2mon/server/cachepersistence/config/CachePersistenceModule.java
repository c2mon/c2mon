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
package cern.c2mon.server.cachepersistence.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * This class is responsible for configuring the Spring context and beans for
 * the cache persistence module.
 *
 * @author Justin Lewis Salmon
 * @author Szymon Halastra
 */
@Configuration
@Import({
    ProcessPersistenceConfig.class,
    EquipmentPersistenceConfig.class,
    SubEquipmentPersistenceConfig.class,
    DataTagPersistenceConfig.class,
    ControlTagPersistenceConfig.class,
    RuleTagPersistenceConfig.class,
    AlarmPersistenceConfig.class
})
@ComponentScan("cern.c2mon.server.cachepersistence")
public class CachePersistenceModule {

  private static final String THREAD_NAME_PREFIX = "BatchPersist-";

  @Autowired
  private CachePersistenceProperties properties;

  @Bean
  public ThreadPoolTaskExecutor cachePersistenceThreadPoolTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(properties.getNumExecutorThreads());
    executor.setMaxPoolSize(properties.getNumExecutorThreads());
    executor.setKeepAliveSeconds(properties.getKeepAliveSeconds());
    executor.setQueueCapacity(properties.getQueueCapacity());
    executor.setAllowCoreThreadTimeOut(true);
    executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
    executor.initialize();
    return executor;
  }
}
