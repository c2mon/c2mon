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
package cern.c2mon.server.daqcommunication.in.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * This class is responsible for configuring the Spring context for the
 * daqcommunication-in module.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
@Import({
    DaqCommunicationInJmsConfig.class
})
@EnableConfigurationProperties(DaqCommunicationInProperties.class)
@ComponentScan("cern.c2mon.server.daqcommunication.in")
public class DaqCommunicationInModule {

  private static final String THREAD_NAME_PREFIX = "TagUpdater-";

  @Autowired
  private DaqCommunicationInProperties properties;

  @Bean
  public ThreadPoolTaskExecutor daqCommunicationThreadPoolTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(properties.getJms().getUpdate().getNumExecutorThreads());
    executor.setMaxPoolSize(properties.getJms().getUpdate().getNumExecutorThreads());
    executor.setKeepAliveSeconds(properties.getJms().getUpdate().getKeepAliveSeconds());
    executor.setAllowCoreThreadTimeOut(true);
    executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
    return executor;
  }
}
