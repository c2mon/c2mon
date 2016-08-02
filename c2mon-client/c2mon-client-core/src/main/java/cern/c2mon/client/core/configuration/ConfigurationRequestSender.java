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
package cern.c2mon.client.core.configuration;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.common.serialisation.HardwareAddressSerializer;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.util.jms.JmsSender;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * JMS sender class for sending the Configuration request to the server and
 * waiting for the response.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class ConfigurationRequestSender {

  private static final long DEFAULT_TIMEOUT = 3_600_000L;

  @Autowired
  private JmsSender jmsSender;

  @Autowired
  private C2monClientProperties properties;

  private ObjectMapper mapper;

  public ConfigurationRequestSender() {
    mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(HardwareAddress.class, new HardwareAddressSerializer());
    mapper.registerModule(module);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  /**
   * @param configuration The configuration created from the client
   * @param listener The listener for the configuration
   *
   * @return The report of the configuration
   */
  public ConfigurationReport applyConfiguration(Configuration configuration, ClientRequestReportListener listener) {
    try {
      String message = mapper.writeValueAsString(configuration);
      String destination = properties.getJms().getConfigQueue();
      String reply = jmsSender.sendRequestToQueue(message, destination, DEFAULT_TIMEOUT);

      if (reply != null) {
        return mapper.readValue(reply, ConfigurationReport.class);
      } else {
        ConfigurationReport failureReport = new ConfigurationReport();
        failureReport.setStatus(ConfigConstants.Status.FAILURE);
        failureReport.setStatusDescription("Server timed out after " + DEFAULT_TIMEOUT + "ms");
        return failureReport;
      }

    } catch (IOException e) {
      ConfigurationReport failureReport = new ConfigurationReport();
      failureReport.setExceptionTrace(e);
      failureReport.setStatus(ConfigConstants.Status.FAILURE);
      failureReport.setStatusDescription("Failed to deserialize response");

      return failureReport;
    }
  }
}

