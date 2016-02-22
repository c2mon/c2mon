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
package cern.c2mon.server.lifecycle;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
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
 * c2mon.properties.location   Location of the c2mon properties file. If not specified, built-in
 *                             properties are used (in-memory database, in-memory JMS broker, etc.).
 * logging.config              Location of the logging configuration file. If not specified, logging
 *                             is done directly to the console.
 * logging.path                Location of the root logging directory.
 *
 *
 * Deprecated properties:
 *
 * c2mon.home                  Home directory of the server (usually the installation directory
 *                             containing bin/, conf/, log/ etc.
 * c2mon.modules.location      Location of the server module descriptor. This file lists the modules
 *                             that the server should run.
 * testMode                    Starts the server in test mode. Accepts all incoming updates no matter
 *                             the PIK and allows normal startup of test DAQs ignoring production
 *                             DAQs updates (true/false)
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

  private static final String C2MON_HOME = "c2mon.home";
  private static final String C2MON_MODULES_LOCATION = "c2mon.modules.location";

  public static void main(final String[] args) throws IOException {
    String home = getProperty(C2MON_HOME);
    if (home == null) {
      home = deduceHomeDirectory();
      if (home == null) {
        throw new RuntimeException(format("Please specify the C2MON home directory using '%s'.", C2MON_HOME));
      }
      setProperty(C2MON_HOME, home);
      log.info(format("Using home directory: %s. To override, use '%s'", home, C2MON_HOME));
    }

    if (getProperty(C2MON_MODULES_LOCATION) == null) {
      setProperty(C2MON_MODULES_LOCATION, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    }

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

  /**
   * Attempt to figure out a sensible location for the application home directory (i.e. the directory containing bin/ conf/ log/ etc.).
   *
   * @return the deduced home directory path, or null if no sensible directory was found
   *
   * @throws IOException
   */
  private static String deduceHomeDirectory() throws IOException {
    String location = ServerStartup.class.getProtectionDomain().getCodeSource().getLocation().getPath();

    // The path can be a file if we are inside a jar
    if (new File(location).isFile()) {
      location = new File(location).getParent();
    }

    Path homeDirectory = Paths.get(location.concat("/../"));
    if (homeDirectory.toFile().exists()) {
      return homeDirectory.toRealPath().toString();
    } else {
      return null;
    }
  }
}
