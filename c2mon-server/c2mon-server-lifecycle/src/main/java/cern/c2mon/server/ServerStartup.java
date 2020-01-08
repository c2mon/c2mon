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
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;

import java.io.IOException;

/**
 * This class is responsible for bootstrapping the C2MON application server.
 *
 * The following optional system properties are available:
 *
 * c2mon.server.properties  Location of an external properties file.
 * logging.config           Location of a custom logging configuration file.
 * logging.path             Location of the root logging directory.
 *
 * @author Justin Lewis Salmon
 */
@SpringBootApplication(exclude = {
  JmsAutoConfiguration.class,
  ActiveMQAutoConfiguration.class,
  DataSourceAutoConfiguration.class,
  DataSourceTransactionManagerAutoConfiguration.class
})
@Slf4j
public class ServerStartup {

  public static void main(final String[] args) throws IOException {
    SpringApplication.run(ServerStartup.class, args);

    log.info("C2MON server is now initialised");
  }
}
