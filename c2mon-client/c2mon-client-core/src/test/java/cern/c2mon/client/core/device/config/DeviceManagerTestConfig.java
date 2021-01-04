/*******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.client.core.device.config;

import org.easymock.EasyMock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import cern.c2mon.client.core.cache.BasicCacheHandler;
import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.client.core.device.cache.DeviceCache;
import cern.c2mon.client.core.device.request.DeviceRequestHandler;
import cern.c2mon.client.core.service.CommandService;
import cern.c2mon.client.core.service.TagService;

@Configuration
@ConditionalOnProperty(prefix= "c2mon.client.device.test", name="mock")
@EnableConfigurationProperties(C2monClientProperties.class)
@ComponentScan({
    "cern.c2mon.client.core.device"
})
public class DeviceManagerTestConfig {

  @Bean
  public TagService tagService() {
    return EasyMock.createMock(TagService.class);
  }
  
  @Bean
  public CommandService commandService() {
    return EasyMock.createMock(CommandService.class);
  }
  
  @Bean
  public BasicCacheHandler dataTagCache() {
    return EasyMock.createMock(BasicCacheHandler.class);
  }
  
  @Bean @Primary
  public DeviceCache deviceCache() {
    return EasyMock.createMock(DeviceCache.class);
  }
  
  @Bean @Primary
  public DeviceRequestHandler requestHandler() {
    return EasyMock.createMock(DeviceRequestHandler.class);
  }
}
