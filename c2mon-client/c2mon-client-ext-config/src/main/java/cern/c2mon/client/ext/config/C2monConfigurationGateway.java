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
package cern.c2mon.client.ext.config;

import cern.c2mon.client.core.C2monServiceGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Justin Lewis Salmon
 */
@Slf4j
public class C2monConfigurationGateway {

  private static final String APPLICATION_SPRING_XML_PATH = "config.xml";

  private static ApplicationContext context;

  private static ConfigurationService configurationService = null;

  /**
   * @return
   */
  public static synchronized ConfigurationService getConfigurationService() {
    if (configurationService == null) {
      initialize();
    }

    return configurationService;
  }

  /**
   *
   */
  private static void initialize() {
    if (C2monServiceGateway.getApplicationContext() == null) {
      C2monServiceGateway.startC2monClientSynchronous();
    }

    if (context == null) {
      context = new ClassPathXmlApplicationContext(new String[]{APPLICATION_SPRING_XML_PATH}, C2monServiceGateway.getApplicationContext());
      configurationService = context.getBean(ConfigurationService.class);
    } else {
      log.warn("ConfigurationService is already initialized.");
    }
  }
}
