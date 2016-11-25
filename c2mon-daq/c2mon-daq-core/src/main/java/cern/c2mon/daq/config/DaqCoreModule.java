/*******************************************************************************
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
 ******************************************************************************/
package cern.c2mon.daq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

/**
 * This configuration class is responsible for importing an externalised
 * properties file specified by c2mon.daq.properties.location (if it exists)
 * and using it to override the default properties embedded inside the package.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
@Import({
    JmsConfig.class,
    ProcessMessageSenderConfig.class,
    ProcessRequestSenderConfig.class,
    DeadbandConfig.class,
})
@ComponentScan("cern.c2mon.daq.common")
@EnableConfigurationProperties(DaqProperties.class)
@PropertySource(value = "${c2mon.daq.properties}", ignoreResourceNotFound = true)
@Slf4j
public class DaqCoreModule {

  @Bean
  public InitializingBean showJmsMode(DaqProperties properties) {
    return () -> log.info("The following JMS mode is active: {}", properties.getJms().getMode());
  }
}
