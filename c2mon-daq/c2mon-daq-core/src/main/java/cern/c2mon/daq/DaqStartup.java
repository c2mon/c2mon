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

import static java.lang.String.format;
import static java.lang.System.getProperty;

import java.io.IOException;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

import cern.c2mon.daq.config.Options;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible for bootstrapping a C2MON DAQ process.
 *
 * @author Justin Lewis Salmon
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = {JmsAutoConfiguration.class, ActiveMQAutoConfiguration.class})
@Slf4j
public class DaqStartup {

  public static void main(String[] args) throws IOException {
    String daqName = getProperty(Options.C2MON_DAQ_NAME);
    if (daqName == null) {
      throw new RuntimeException(format("Please specify the DAQ process name using '%s'.", Options.C2MON_DAQ_NAME));
    }

    // The JMS mode (single, double, test) is controlled via Spring profiles
    String mode = getProperty(Options.JMS_MODE);
    System.setProperty("spring.profiles.active", mode == null ? "single" : mode);

    new SpringApplicationBuilder(DaqStartup.class).bannerMode(Banner.Mode.OFF).build().run(args);
  }
}
