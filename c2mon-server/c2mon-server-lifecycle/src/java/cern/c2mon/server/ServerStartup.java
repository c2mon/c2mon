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
package cern.c2mon.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.SmartLifecycle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;

/**
 * This class is responsible for bootstrapping the C2MON application server.
 *
 * The following optional system properties are available:
 *
 * c2mon.server.properties  Location of an external properties file.
 * logging.config           Location of a custom logging configuration file.
 * logging.path             Location of the root logging directory.
 *
 *
 * Deprecated properties:
 *
 * testMode                 Starts the server in test mode. Accepts all
 *                          incoming updates no matter the PIK and allows
 *                          normal startup of test DAQs ignoring production
 *                          DAQs updates (true/false)
 *
 * @author Justin Lewis Salmon
 * @author Mark Brightwell
 * @author Nacho Vilches
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = {
    JmsAutoConfiguration.class,
    ActiveMQAutoConfiguration.class,
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class
})
@Slf4j
public class ServerStartup {

  public static void main(final String[] args) throws IOException {

    // TODO: rename this and put it in c2mon.properties. "test mode" is not very descriptive.
    if ((getProperty("testMode")) != null && (getProperty("testMode").equals("true"))) {
      log.info("C2MON server starting up in TEST mode");
    }

    // Run the application
    ConfigurableApplicationContext context = SpringApplication.run(ServerStartup.class, args);

    /**
     * Currently the context needs to be manually started. This could maybe be removed by playing around with
     * {@link SmartLifecycle#isAutoStartup()}.
     */
    context.start();
    context.registerShutdownHook();
  }
}
