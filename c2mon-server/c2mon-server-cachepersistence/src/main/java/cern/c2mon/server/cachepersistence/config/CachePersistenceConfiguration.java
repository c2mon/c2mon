/**
 * *****************************************************************************
 * * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * *
 * * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * * C2MON is free software: you can redistribute it and/or modify it under the
 * * terms of the GNU Lesser General Public License as published by the Free
 * * Software Foundation, either version 3 of the license.
 * *
 * * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * * more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package cern.c2mon.server.cachepersistence.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * This class is responsible for configuring the Spring Context and beans used by CacheManager
 * for cache-persistence module. It is automatically detected.
 *
 * @author Szymon Halastra
 */
@Configuration
@ComponentScan("cern.c2mon.server.cachepersistence")
public class CachePersistenceConfiguration {

  @Value("${c2mon.server.cachepersistence.batchpersistence.numExecutorThreads}")
  private int corePoolSize;

  @Value("${c2mon.server.cachepersistence.batchpersistence.numExecutorThreads}")
  private int maxPoolSize;

  @Value("${c2mon.server.cachepersistence.batchpersistence.keepAliveSeconds}")
  private int threadIdleLimit;

  @Value("${c2mon.server.cachepersistence.batchpersistence.queueCapacity}")
  private int queueCapacity;

  private static final String THREAD_NAME_PREFIX = "BatchPersist-";

  /**
   * Bean responsible for creating custom ThreadPool with custom thread name prefix
   *
   * @return Spring ThreadPoolTaskExecutor with custom thread name prefix
   */
  @Bean
  public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setKeepAliveSeconds(threadIdleLimit);
    executor.setQueueCapacity(queueCapacity);

    executor.setAllowCoreThreadTimeOut(true);

    executor.setThreadNamePrefix(THREAD_NAME_PREFIX);

    return executor;
  }
}
