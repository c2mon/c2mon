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

package cern.c2mon.server.daqcommunication.in.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Szymon Halastra
 */
@Configuration
@ComponentScan("cern.c2mon.server.daqcommunication.in")
public class DaqCommunicationInConfiguration {

  @Value("${c2mon.server.daqcommunication.jms.update.numExecutorThreads}")
  private int corePoolSize;

  @Value("${c2mon.server.daqcommunication.jms.update.numExecutorThreads}")
  private int maxPoolSize;

  private static final int THREAD_IDLE_LIMIT = 60;
  private static final String THREAD_NAME_PREFIX = "TagUpdate-";

  @Bean(name = "threadPoolTaskExecutor")
  public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setKeepAliveSeconds(THREAD_IDLE_LIMIT);

    executor.setAllowCoreThreadTimeOut(true);

    executor.setThreadNamePrefix(THREAD_NAME_PREFIX);

    return executor;
  }
}
