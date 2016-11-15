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
package cern.c2mon.client.core.config;

import java.io.IOException;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

/**
 * Listens for the {@link ApplicationEnvironmentPreparedEvent} and injects
 * ${c2mon.client.conf.url} into the environment with the highest precedence
 * (if it exists). This is in order to allow users to point to an external
 * properties file via ${c2mon.client.conf.url}.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
public class C2monApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

  @Override
  public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
    ConfigurableEnvironment environment = event.getEnvironment();
    String propertySource = environment.getProperty("c2mon.client.conf.url");

    if (propertySource != null) {
      try {
        environment.getPropertySources().addAfter("systemEnvironment", new ResourcePropertySource(propertySource));
      } catch (IOException e) {
        throw new RuntimeException("Could not read property source", e);
      }
    }
  }
}

