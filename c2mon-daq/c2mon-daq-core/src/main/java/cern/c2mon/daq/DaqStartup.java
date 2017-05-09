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
package cern.c2mon.daq;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.AbstractEnvironment;

import cern.c2mon.daq.common.DriverKernel;
import cern.c2mon.daq.config.DaqCoreModule;

import static java.lang.System.getProperty;

/**
 * This class is responsible for bootstrapping a C2MON DAQ process.
 *
 * @author Justin Lewis Salmon
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = {JmsAutoConfiguration.class, ActiveMQAutoConfiguration.class, DataSourceAutoConfiguration.class})
@Import({
    DaqCoreModule.class
})
@Slf4j
public class DaqStartup {

  private static SpringApplication application = null;
  private static ConfigurableApplicationContext context = null;
  private static DriverKernel driverKernel;


  public static void main(String[] args) throws IOException {
    start(args);
  }

  public static synchronized void start(String[] args) throws IOException {
    String daqName = getProperty("c2mon.daq.name");
    if (daqName == null) {
      throw new RuntimeException("Please specify the DAQ process name using 'c2mon.daq.name'");
    }

    // The JMS mode (single, double, test) is controlled via Spring profiles
    String mode = getProperty("c2mon.daq.jms.mode");
    if (mode != null) {
      System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, mode);
    }

    if (application == null) {
      application = new SpringApplicationBuilder(DaqStartup.class)
              .bannerMode(Banner.Mode.OFF)
              .build();
    }
    context = application.run(args);

    driverKernel = context.getBean(DriverKernel.class);
    driverKernel.init();

    log.info("DAQ core is now initialized");
  }

  public static synchronized void stop() {
    try {
      log.info("Stopping DAQ process...");
      if (driverKernel != null) {
        driverKernel.shutdown();
      }
      if (context.isRunning()) {
        context.close();
      }
    } catch (Exception ex) {
      log.error("Error occured whilst gradually stopping DAQ process", ex);
    }
  }

}
